package cecs429.index;

import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.query.BooleanQueryParser;
import cecs429.text.EnglishTokenStream;
import cecs429.text.TokenProcessor;

import javax.xml.crypto.Data;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SinglePassInMemoryIndexWriter {


    private final int MAX_ALLOWED_MEMORY =500;
    private final String BUCKET_PATH ="\\buckets\\bucket_";

    private PositionalInvertedIndex index;
    private List<String> vocabualary;
    private DiskIndexWriter indexWriter;


    public SinglePassInMemoryIndexWriter(){
        index = new PositionalInvertedIndex();
        vocabualary = new ArrayList<>();
        indexWriter= new DiskIndexWriter();
    }


    public void indexCorpus(DocumentCorpus corpus, TokenProcessor tokenProcessor,Path path) throws IOException{

        int iAllowedMemeory = 0;
        int iBucketNum = 0;
        for(Document document:corpus.getDocuments()) {
            int position = 0;
            EnglishTokenStream tokenStream = new EnglishTokenStream(document.getContent());


                    for(String token : tokenStream.getTokens()){
                        for(String term: tokenProcessor.processToken(token)){
                            if (iAllowedMemeory < MAX_ALLOWED_MEMORY) {
                                iAllowedMemeory++;
                                index.addTerm(term, document.getId(), position++);
                        }
                        else{

                                indexWriter.writeIndex(index, Paths.get(path.toAbsolutePath()+BUCKET_PATH+iBucketNum++).toAbsolutePath());
                                iBucketNum++;
                                index = new PositionalInvertedIndex();
                                vocabualary  = new ArrayList<>();
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
            vocabualary = Stream.concat(vocabualary.stream().sorted(), readVocabfromBucket(path, currentBucket).stream())
                    .distinct()
                    .collect(Collectors.toList());


        }

    }


    //create mereged postings list
    private void mergeBucketsPostings (Path path,int numBuckets) throws IOException{


        int currentBucket = 0;
        List<DataInputStream> bucketVocabStreams = new ArrayList<>();
        List<DataInputStream> bucketPostingsStreams = new ArrayList<>();
        List<DataInputStream> bucketVocabTableStreams = new ArrayList<>();

        //create input streams for all the buckets
        bucketVocabStreams.add(new DataInputStream(new FileInputStream(path.toAbsolutePath() + BUCKET_PATH + currentBucket + "\\vocabs.bin")));
        bucketPostingsStreams.add(new DataInputStream(new FileInputStream(path.toAbsolutePath() + BUCKET_PATH + currentBucket + "\\postings.bin")));
        bucketVocabTableStreams.add(new DataInputStream(new FileInputStream(path.toAbsolutePath() + BUCKET_PATH + currentBucket + "\\vocabTable.bin")));


        List<List<String>> bucketTerms = new ArrayList<>();

        //we also remember the last ending position, postings positions to avoid going back to read it in the vocab table;
        //check if this can cause memory overflow!!
        //*********ON HOLD FOR NOW*********************************
        //List<List<Long>> lastPositions = new ArrayList<>();  //is this not needed??


        List<List<Long>> postingsPositions =new ArrayList<>(); //we will store postings positions as we scan the vocab table to avoid going back

        while (currentBucket<numBuckets) {
            readNTermsFromBucket(bucketVocabStreams,bucketVocabTableStreams,bucketTerms,postingsPositions,currentBucket,5);
            currentBucket++;
        }


        //TODO: auto create this folder.


        DataOutputStream streamOutPostings = new DataOutputStream(new FileOutputStream(path.toAbsolutePath().toString()+"\\index\\postings.bin"));
        DataOutputStream streamOutVocabTable = new DataOutputStream(new FileOutputStream(path.toAbsolutePath().toString()+"\\index\\vocabTable.bin"));


        for(String term:vocabualary){
            //create list L
            //add terms to it
            //write it to the list

            List<Integer> mergedPositions = new ArrayList<>();
            List<Integer> mergedDocumentIds = new ArrayList<>();

            int iBucketCnt = 0;

            while(iBucketCnt<numBuckets){
                int vocabPosition = 0;
                if(term.equals(bucketTerms.get(iBucketCnt).get(0))){
                    //copy all the postings for this location from the bucket to our main file.

                    DataInputStream streamIn = bucketPostingsStreams.get(currentBucket);
                    //streamIn.skip(postingsPositions.get(currentBucket).get(0));
                    int documentFreq = streamIn.readInt();
                    int currentDoc= 0;
                    while(currentDoc<documentFreq){

                        mergedDocumentIds.add(streamIn.readInt());

                        int currentPos = 0;
                        int termFreq = streamIn.readInt();
                        while(currentPos<termFreq){

                            mergedPositions.add(streamIn.readInt());
                            currentPos++;
                        }
                        currentDoc++;

                    }


                    bucketTerms.get(iBucketCnt).remove(0);
                    postingsPositions.get(iBucketCnt).remove(0);

                    if(bucketTerms.size() == 0){
                         readNTermsFromBucket(bucketVocabStreams,bucketVocabTableStreams,bucketTerms,postingsPositions,currentBucket,5);
                    }


                }

            }

            //start the method here
            writePostingsMerged(mergedDocumentIds,mergedDocumentIds,term);

        }

    }



    private void readNTermsFromBucket(List<DataInputStream> bucketVocabStreams,List<DataInputStream> bucketVocabTableStreams, List<List<String>> bucketTerms,List<List<Long>> postingsPositions,int currentBucket,int termCount)throws IOException{

        while (termCount < 5) {
            long start = bucketVocabTableStreams.get(currentBucket).readLong();
            long posting = bucketVocabTableStreams.get(currentBucket).readLong();
            long end = bucketVocabTableStreams.get(currentBucket).readLong();



            String term = "";

            while (start <= end) {

                term+=bucketVocabStreams.get(currentBucket).readByte();
                start++;
            }

            if (termCount == 0) {
//                List<Long> list = new ArrayList<>();
//                list.add(end);
//                lastPositions.add(list);

                List<Long> list = new ArrayList<>();
                list.add(posting);
                postingsPositions.add(list);

                List<String> terms = new ArrayList<>();
                terms.add(term);
                bucketTerms.add(terms);
            } else {

                //lastPositions.get(currentBucket).add(end);
                postingsPositions.get(currentBucket).add(posting);
                bucketTerms.get(currentBucket).add(term);
            }
            termCount++;
        }

    }
    private HashMap<String,Long> writePostingsMerged(Path path) throws IOException {

        return null;
    }


    private List<String> readVocabfromBucket(Path path,int bucket) throws IOException{

        DataInputStream streamVocab = new DataInputStream(new FileInputStream(path.toAbsolutePath()+BUCKET_PATH+bucket+"\\vocabs.bin"));
        DataInputStream streamVocabTable =new DataInputStream(new FileInputStream(path.toAbsolutePath()+BUCKET_PATH+bucket+"\\vocabTable.bin"));
        List<String> bucketVocab = new ArrayList<>();
        while(streamVocabTable.available()>0){

            long start = streamVocabTable.readLong();
            streamVocabTable.readLong();
            long end = streamVocabTable.readLong();
            streamVocabTable.readLong();

            String term = "";
            while(start<=end){
                term+= streamVocab.readByte();
                start++;
            }
            bucketVocab.add(term);
        }

        return bucketVocab;
    }


}
