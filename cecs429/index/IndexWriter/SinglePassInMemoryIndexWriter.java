package cecs429.index.IndexWriter;

import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.index.PositionalInvertedIndex;
import cecs429.text.EnglishTokenStream;
import cecs429.text.TokenProcessor;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SinglePassInMemoryIndexWriter {


    private final int MAX_ALLOWED_MEMORY =4194304;
    private final String BUCKET_PATH ="\\buckets\\bucket_";

    private PositionalInvertedIndex index;
    private List<String> vocabulary;
    private DiskIndexWriter indexWriter;


    public SinglePassInMemoryIndexWriter(){
        index = new PositionalInvertedIndex();
        vocabulary = new ArrayList<>();
        indexWriter= new DiskIndexWriter();
    }


    public void indexCorpus(DocumentCorpus corpus, TokenProcessor tokenProcessor,Path path) throws IOException{

        int iAllowedMemeory =0;
        int iBucketNum = 0;
          for(Document document:corpus.getDocuments()) {
            int position = 0;
           EnglishTokenStream tokenStream = new EnglishTokenStream(document.getContent());


                    for(String token : tokenStream.getTokens()){
                        for(String term: tokenProcessor.processToken(token)){

                            if(term.equals(""))
                            {
                                continue;
                            }

                            if (iAllowedMemeory < MAX_ALLOWED_MEMORY) {
                                iAllowedMemeory++;
                                index.addTerm(term, document.getId(), position++);
                        }
                        else{

                                indexWriter.writeIndex(index, Paths.get(path.toAbsolutePath()+BUCKET_PATH+iBucketNum++).toAbsolutePath());
                                index = new PositionalInvertedIndex();
                                vocabulary = new ArrayList<>();
                                iAllowedMemeory=0;


                            }

                    }
            }
        }
        if(iAllowedMemeory!=0) // we still have terms left to put in the buckets
            indexWriter.writeIndex(index, Paths.get(path.toAbsolutePath()+BUCKET_PATH+iBucketNum++).toAbsolutePath());


        mergeBucketsVocab(path,iBucketNum);
        mergeBucketsPostings(path,iBucketNum);

    }

    //combine the whole vocab by unioning vocabs in each bucket
    private void mergeBucketsVocab(Path path,int numBuckets) throws IOException {



        int currentBucket =0;
        while (currentBucket < numBuckets) {

            DataInputStream streamVocab = new DataInputStream(new FileInputStream(path.toAbsolutePath()+BUCKET_PATH+currentBucket+"\\vocabs.bin"));
            DataInputStream streamVocabTable =new DataInputStream(new FileInputStream(path.toAbsolutePath()+BUCKET_PATH+currentBucket+"\\vocabTable.bin"));

            vocabulary = Stream.concat(vocabulary.stream(), readVocabfromBucket(streamVocabTable,streamVocab, currentBucket).stream())
                    .distinct().sorted()
                    .collect(Collectors.toList());
    currentBucket++;
    streamVocab.close();
    streamVocabTable.close();

        }

    }


    //create mereged postings list
    private void mergeBucketsPostings (Path path,int numBuckets) throws IOException{


         int currentBucket = 0;

        List<List<String>> bucketTerms = new ArrayList<>();

        //we also remember the last ending position, postings positions to avoid going back to read it in the vocab table;
        //check if this can cause memory overflow!!

        List<DataInputStream> bucketVocabStreams = new ArrayList<>();
        List<DataInputStream> bucketPostingsStreams = new ArrayList<>();
        List<DataInputStream> bucketVocabTableStreams = new ArrayList<>();

        //we will store the last ending position of each file to avoid going back in the file
        List<Long> lastEndPositions = new ArrayList<>();
        List<List<Long>> postingsPositions =new ArrayList<>(); //we will store postings positions as we scan the vocab table to avoid going back

        while (currentBucket<numBuckets) {

            //create input streams for all the buckets
            bucketVocabStreams.add(new DataInputStream(new FileInputStream(path.toAbsolutePath() + BUCKET_PATH + currentBucket + "\\vocabs.bin")));
            bucketPostingsStreams.add(new DataInputStream(new FileInputStream(path.toAbsolutePath() + BUCKET_PATH + currentBucket + "\\postings.bin")));
            bucketVocabTableStreams.add(new DataInputStream(new FileInputStream(path.toAbsolutePath() + BUCKET_PATH + currentBucket + "\\vocabTable.bin")));


            bucketTerms.add(readNTermsFromBucket(bucketVocabStreams,bucketVocabTableStreams,postingsPositions,currentBucket,lastEndPositions,5));
            currentBucket++;
        }


        //TODO: auto create this folder.


        HashMap<Integer,List<Integer>> documentWeights = new HashMap<>(); //maps document id to list of tfd's.

        indexWriter.initDBStore(path);
        indexWriter.initMergedPostingsStream(path);

        for(String term: vocabulary){
            //create list L
            //add terms to it
            //write it to the list

            HashMap<Integer,List<Integer>> mergedPositions = new HashMap<>();
            List<Integer> mergedDocumentIds = new ArrayList<>();

            int iBucketCnt = 0;

            while(iBucketCnt<numBuckets){
                if(bucketTerms.get(iBucketCnt).size()>0){
                    if (term.equals(bucketTerms.get(iBucketCnt).get(0))) {
                        //copy all the postings for this location from the bucket to our main file.

                        DataInputStream streamIn = bucketPostingsStreams.get(iBucketCnt);
                        //streamIn.skip(postingsPositions.get(currentBucket).get(0));
                        int documentFreq = streamIn.readInt();
                        int currentDoc = 0;
                        while (currentDoc < documentFreq) {


                            int docID = streamIn.readInt();
                            if (mergedDocumentIds.size() == 0)
                                mergedDocumentIds.add(docID);
                            else if (mergedDocumentIds.get(mergedDocumentIds.size() - 1) != docID)
                                mergedDocumentIds.add(docID);

                            int currentPos = 0;
                            int termFreq = streamIn.readInt();

                            while (currentPos < termFreq) {

                                if (mergedPositions.containsKey(docID)) {
                                    mergedPositions.get(docID).add(streamIn.readInt());
                                } else {
                                    List<Integer> posList = new ArrayList<>();
                                    posList.add(streamIn.readInt());
                                    mergedPositions.put(docID, posList);
                                }
                                currentPos++;
                            }
                            currentDoc++;

                        }


                        bucketTerms.get(iBucketCnt).remove(0);
                        postingsPositions.get(iBucketCnt).remove(0);

                        if (bucketTerms.get(iBucketCnt).size() == 0) {
                            //handle the case when no more terms are left to be read in a bucket
                            bucketTerms.set(iBucketCnt, readNTermsFromBucket(bucketVocabStreams, bucketVocabTableStreams, postingsPositions, iBucketCnt, lastEndPositions, 5));
                        }


                    }
                }
                iBucketCnt++;
            }


            if(mergedDocumentIds.size()>0) {
                indexWriter.writeMergedVocab(term);
                indexWriter.writeMergedPostings(mergedDocumentIds, mergedPositions);
            }

            //check if the term exists in the document id already exists in the document weight hashmap and handle adding to its list
            for(int docID : mergedDocumentIds) {
                if (documentWeights.containsKey(docID)) {
                    List<Integer> weightList = documentWeights.get(docID);
                    weightList.add(mergedPositions.get(docID).size());


                } else {
                    List<Integer> weightList = new ArrayList<>();
                    weightList.add(mergedPositions.get(docID).size());
                    documentWeights.put(docID, weightList);

                }
            }

          /*  System.out.println("-------------------------Postings for :'"+term+"'---------------------------------");
            for(int doc:mergedDocumentIds){

                System.out.println("id:"+doc);
                System.out.print(" pos : ");
                for(int pos:mergedPositions.get(doc)){
                    System.out.print(" "+pos+",");
                }
                System.out.println("");
            }
            System.out.println("");
           System.out.println("----------------------------------------------------------------------------------------");
*/


        }
        indexWriter.closeDBStore();

        writeDocumentWeights(documentWeights,path);


        for(DataInputStream stream : bucketVocabStreams)
            stream.close();
        for(DataInputStream stream : bucketPostingsStreams)
            stream.close();
        for(DataInputStream stream : bucketVocabTableStreams)
            stream.close();


    }


    private void writeDocumentWeights(HashMap<Integer,List<Integer>> documentWeights,Path path) throws IOException{
        FileOutputStream fos = new FileOutputStream(path.toAbsolutePath().toString()+"\\index\\docWeights.bin");
        DataOutputStream stream = new DataOutputStream(fos);
        for(int docID:documentWeights.keySet()){

            if(documentWeights.containsKey(docID)) {
                List<Integer> documentTermFreqeuncyList = documentWeights.get(docID);
                Double ld = new Double(0);

                for (Integer freq : documentTermFreqeuncyList) {

                    Double log = 1 + Math.log(freq);
                    ld += log * log;


                }
                Double ldsqrt = Math.sqrt(ld);
                stream.writeDouble(ldsqrt);
            }
            else{

                stream.writeDouble(0);
            }



        }
        stream.close();
    }
    private List<String> readNTermsFromBucket(List<DataInputStream> bucketVocabStreams,List<DataInputStream> bucketVocabTableStreams,List<List<Long>> postingsPositions,int currentBucket,List<Long> lastEndPos,int termCount)throws IOException{

        List<String> bucketTerms = new ArrayList<>();
        int currentTermCount =0;
        long start=0;
        long posting=0;
        if(lastEndPos.size()-1<currentBucket)
            start= bucketVocabTableStreams.get(currentBucket).readLong();
        else
            start = lastEndPos.get(currentBucket);

        long end=0;
        while (currentTermCount < termCount) {

            if(bucketVocabTableStreams.get(currentBucket).available()>0) {
                posting = bucketVocabTableStreams.get(currentBucket).readLong();
            }

            if(bucketVocabTableStreams.get(currentBucket).available()>0)
                end = bucketVocabTableStreams.get(currentBucket).readLong();
            else
                end = -1;



            String term = "";
            //read utf here
          /*  while (start < end && bucketVocabStreams.get(currentBucket).available()>0) {

                term+=(char)bucketVocabStreams.get(currentBucket).readByte();
                start++;
            }*/

          if(bucketVocabStreams.get(currentBucket).available()>0)
          term = bucketVocabStreams.get(currentBucket).readUTF();
          else
              break;

            if (currentTermCount == 0) {
//                List<Long> list = new ArrayList<>();
//                list.add(end);
//                lastPositions.add(list);

                List<Long> list = new ArrayList<>();
                list.add(posting);
                if(postingsPositions.size()-1 <currentBucket)
                    postingsPositions.add(list);
                else
                    postingsPositions.set(currentBucket,list);
                bucketTerms.add(term);


            } else {

                //lastPositions.get(currentBucket).add(end);
                postingsPositions.get(currentBucket).add(posting);
                bucketTerms.add(term);
            }
            currentTermCount++;
            start = end;
        }

        if(lastEndPos.size()-1<currentBucket)
            lastEndPos.add(end);
        else {

            lastEndPos.set(currentBucket,end);
        }

        return bucketTerms;
    }


    private List<String> readVocabfromBucket(DataInputStream streamVocabTable,DataInputStream streamVocab,int bucket) throws IOException{

        List<String> bucketVocab = new ArrayList<>();
        long start=0;
        long end=-1;
        while(streamVocab.available()>0){


          /* if(end ==-1)
            {
                start = streamVocabTable.readLong();
                streamVocabTable.readLong();
            }

            if(streamVocabTable.available()>0) {
                end = streamVocabTable.readLong();
                if(end == 5219)
                    System.out.print("fishy");
                streamVocabTable.readLong();
            }
            else
                end = start-1; //last term so we read till the end of file


            String term = "";
            /*while(start<end  && streamVocab.available()>0){
                term+= (char)streamVocab.readUTF();
                start++;
            }

            String term =streamVocab.readUTF();

            bucketVocab.add(term);
            term="";
            start = end;*/
          bucketVocab.add(streamVocab.readUTF());
        }

        /*String term = "";
        //read the last remaining word
        while(streamVocab.available()>0){
            term+= (char)streamVocab.readByte();

        }*/
       // bucketVocab.add(term);
        streamVocab.close();
        streamVocabTable.close();
        return bucketVocab;
    }


}
