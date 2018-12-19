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


    private final int MAX_ALLOWED_MEMORY = 4194304;
    private final String BUCKET_PATH = "\\buckets\\bucket_";

    private PositionalInvertedIndex index;
    private List<String> vocabulary;
    private DiskIndexWriter indexWriter;


    public SinglePassInMemoryIndexWriter() {
        index = new PositionalInvertedIndex();
        vocabulary = new ArrayList<>();
        indexWriter = new DiskIndexWriter();
    }


    public void indexCorpus(DocumentCorpus corpus, TokenProcessor tokenProcessor, Path path) throws IOException {
        //we also need to write pre-stemmed vocabs on to the disk so we can process them for K-Gram
        int iAllowedMemeory = 0;
        int iBucketNum = 0;
        for (Document document : corpus.getDocuments()) {
            int position = 0;
            EnglishTokenStream tokenStream = new EnglishTokenStream(document.getContent());


            for (String token : tokenStream.getTokens()) {
                for (String term : tokenProcessor.processToken(token)) {

                    if (term.equals("")) {
                        continue;
                    }

                    if (iAllowedMemeory < MAX_ALLOWED_MEMORY) {
                        iAllowedMemeory++;
                        index.addTerm(term, document.getId(), position++);
                    } else {

                        indexWriter.writeIndex(index, Paths.get(path.toAbsolutePath() + BUCKET_PATH + iBucketNum++).toAbsolutePath());
                        index = new PositionalInvertedIndex();
                        vocabulary = new ArrayList<>();
                        iAllowedMemeory = 0;


                    }

                }
            }
        }
        if (iAllowedMemeory != 0) // check if we still have terms left to put in the buckets
            indexWriter.writeIndex(index, Paths.get(path.toAbsolutePath() + BUCKET_PATH + iBucketNum++).toAbsolutePath());


        mergeBucketsVocab(path, iBucketNum);
        mergeBucketsPostings(path, corpus, iBucketNum);

        indexWriter.writeUnprocessedVocabs(path, tokenProcessor.getUnStemmedVocabs());

    }

    //combine the whole vocab by unioning vocabs in each bucket
    private void mergeBucketsVocab(Path path, int numBuckets) throws IOException {


        int currentBucket = 0;
        while (currentBucket < numBuckets) {

            DataInputStream streamVocab = new DataInputStream(new FileInputStream(path.toAbsolutePath() + BUCKET_PATH + currentBucket + "\\vocabs.bin"));
            DataInputStream streamVocabTable = new DataInputStream(new FileInputStream(path.toAbsolutePath() + BUCKET_PATH + currentBucket + "\\vocabTable.bin"));

            vocabulary = Stream.concat(vocabulary.stream(), readVocabfromBucket(streamVocabTable, streamVocab, currentBucket).stream())
                    .distinct().sorted()
                    .collect(Collectors.toList());
            currentBucket++;
            streamVocab.close();
            streamVocabTable.close();

        }

    }


    //create mereged postings list
    private void mergeBucketsPostings(Path path, DocumentCorpus corpus, int numBuckets) throws IOException {


        int currentBucket = 0;

        List<List<String>> bucketTerms = new ArrayList<>();

        //we also remember the last ending position, postings positions to avoid going back to read it in the vocab table;

        List<DataInputStream> bucketVocabStreams = new ArrayList<>();
        List<DataInputStream> bucketPostingsStreams = new ArrayList<>();
        List<DataInputStream> bucketVocabTableStreams = new ArrayList<>();

        //we will store the last ending position of each file to avoid going back in the file
        List<Long> lastEndPositions = new ArrayList<>();
        List<List<Long>> postingsPositions = new ArrayList<>(); //we will store postings positions as we scan the vocab table to avoid going back

        while (currentBucket < numBuckets) {

            //create input streams for all the buckets
            bucketVocabStreams.add(new DataInputStream(new FileInputStream(path.toAbsolutePath() + BUCKET_PATH + currentBucket + "\\vocabs.bin")));
            bucketPostingsStreams.add(new DataInputStream(new FileInputStream(path.toAbsolutePath() + BUCKET_PATH + currentBucket + "\\postings.bin")));
            bucketVocabTableStreams.add(new DataInputStream(new FileInputStream(path.toAbsolutePath() + BUCKET_PATH + currentBucket + "\\vocabTable.bin")));


            bucketTerms.add(readNTermsFromBucket(bucketVocabStreams, bucketVocabTableStreams, postingsPositions, currentBucket, lastEndPositions, 5));
            currentBucket++;
        }


        HashMap<Integer, List<Integer>> documentWeights = new HashMap<>(); //maps document id to list of tfd's.

        indexWriter.initDBStore(path);
        indexWriter.initMergedPostingsStream(path);

        for (String term : vocabulary) {
            //create list L
            //add terms to it
            //write it to the list

            HashMap<Integer, List<Integer>> mergedPositions = new HashMap<>();
            List<Integer> mergedDocumentIds = new ArrayList<>();

            int iBucketCnt = 0;

            while (iBucketCnt < numBuckets) {
                if (bucketTerms.get(iBucketCnt).size() > 0) {
                    if (term.equals(bucketTerms.get(iBucketCnt).get(0))) {


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

                        //bucket is empty, read the next N terms from the bucket!
                        if (bucketTerms.get(iBucketCnt).size() == 0) {
                            bucketTerms.set(iBucketCnt, readNTermsFromBucket(bucketVocabStreams, bucketVocabTableStreams, postingsPositions, iBucketCnt, lastEndPositions, 5));
                        }


                    }
                }
                iBucketCnt++;
            }


            if (mergedDocumentIds.size() > 0) {
                indexWriter.writeMergedVocab(term);
                indexWriter.writeMergedPostings(mergedDocumentIds, mergedPositions);
            }

            //check if the term exists in the document id already exists in the document weight hashmap and handle adding to its list
            for (int docID : mergedDocumentIds) {
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

        writeDocumentWeights(documentWeights, corpus, path);


        for (DataInputStream stream : bucketVocabStreams)
            stream.close();
        for (DataInputStream stream : bucketPostingsStreams)
            stream.close();
        for (DataInputStream stream : bucketVocabTableStreams)
            stream.close();


    }


    private void writeDocumentWeights(HashMap<Integer, List<Integer>> documentWeights, DocumentCorpus corpus, Path path) throws IOException {
        FileOutputStream fos = new FileOutputStream(path.toAbsolutePath().toString() + "\\index\\docWeights.bin");
        DataOutputStream stream = new DataOutputStream(fos);
        for (int i = 0; i < corpus.getCorpusSize(); i++) {

            if (documentWeights.containsKey(i)) {
                List<Integer> documentTermFreqeuncyList = documentWeights.get(i);
                Double ld = new Double(0);

                for (Integer freq : documentTermFreqeuncyList) {

                    Double log = 1 + Math.log(freq);
                    ld += log * log;


                }
                Double ldsqrt = Math.sqrt(ld);
                stream.writeDouble(ldsqrt);
                System.out.println(ldsqrt + " for doc " + corpus.getDocument(i).getName());
            } else {

                stream.writeDouble(0);
            }


        }
        stream.close();
    }


    //Read only the next N terms from bucket, used for merging vocabs from all teh buckets
    private List<String> readNTermsFromBucket(List<DataInputStream> bucketVocabStreams, List<DataInputStream> bucketVocabTableStreams, List<List<Long>> postingsPositions, int currentBucket, List<Long> lastEndPos, int termCount) throws IOException {

        List<String> bucketTerms = new ArrayList<>();
        int currentTermCount = 0;
        long start = 0;
        long posting = 0;
        if (lastEndPos.size() - 1 < currentBucket)
            start = bucketVocabTableStreams.get(currentBucket).readLong();
        else
            start = lastEndPos.get(currentBucket);

        long end = 0;
        while (currentTermCount < termCount) {

            if (bucketVocabTableStreams.get(currentBucket).available() > 0) {
                posting = bucketVocabTableStreams.get(currentBucket).readLong();
            }

            if (bucketVocabTableStreams.get(currentBucket).available() > 0)
                end = bucketVocabTableStreams.get(currentBucket).readLong();
            else
                end = -1;


            String term = "";

            if (bucketVocabStreams.get(currentBucket).available() > 0)
                term = bucketVocabStreams.get(currentBucket).readUTF();
            else
                break;

            if (currentTermCount == 0) {

                List<Long> list = new ArrayList<>();
                list.add(posting);
                if (postingsPositions.size() - 1 < currentBucket)
                    postingsPositions.add(list);
                else
                    postingsPositions.set(currentBucket, list);
                bucketTerms.add(term);


            } else {

                //lastPositions.get(currentBucket).add(end);
                postingsPositions.get(currentBucket).add(posting);
                bucketTerms.add(term);
            }
            currentTermCount++;
            start = end;
        }

        if (lastEndPos.size() - 1 < currentBucket)
            lastEndPos.add(end);
        else {

            lastEndPos.set(currentBucket, end);
        }

        return bucketTerms;
    }


    //Reads complete vocab from a bucket, used for unioning vocabs
    private List<String> readVocabfromBucket(DataInputStream streamVocabTable, DataInputStream streamVocab, int bucket) throws IOException {

        List<String> bucketVocab = new ArrayList<>();
        long start = 0;
        long end = -1;
        while (streamVocab.available() > 0) {

            bucketVocab.add(streamVocab.readUTF());
        }


        // bucketVocab.add(term);
        streamVocab.close();
        streamVocabTable.close();
        return bucketVocab;
    }


}
