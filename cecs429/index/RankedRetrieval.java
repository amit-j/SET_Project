package cecs429.index;

import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.query.RankedRetrievalParser;
import cecs429.text.TokenProcessor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.*;

public class RankedRetrieval {

    RandomAccessFile randomAccessDocWeight;

    public RankedRetrieval(Path path) {

        try {
            randomAccessDocWeight = new RandomAccessFile(path.toAbsolutePath().toString() + "\\docWeights.bin", "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public List<Integer> doRankedRetrieval(String query, DocumentCorpus corpus, Index index, TokenProcessor tokenProcessor) {

        RankedRetrievalParser parser = new RankedRetrievalParser();

        //stores docId and accumulator
        Map<Integer, Double> docAccumulatorMap = new HashMap<>();

        //String[] terms = query.split(" ");
        String[] terms = parser.parseQuery(query, tokenProcessor);
        double wdt = 0.0;
        double wqt = 0.0;

        /*Term at a time*/
        for (String term : terms) {
            List<Posting> postings = index.getPostings(term);
            if (postings.size() > 0)
                wqt = Math.log(1 + corpus.getCorpusSize() / postings.size());
            else
                wqt = 0;

            /*For each document from posting for a term calculating wdt and accumulator*/
            for (Posting posting : postings) {

                wdt = 1 + Math.log(posting.getTermFrequency());

                int docId = posting.getDocumentId();
                double accumulator = 0;

                if (docAccumulatorMap.get(docId) == null) {
                    accumulator = wdt * wqt;
                } else {
                    accumulator = docAccumulatorMap.get(docId);
                    accumulator += wdt * wqt;
                }
                docAccumulatorMap.put(docId, accumulator);
                // System.out.println(corpus.getDocument(docId).getName()+" wdt:"+wdt+" ld:"+getDoucumentWeight(docId));
            }
        }

        for (int docId : docAccumulatorMap.keySet()) {

            double accumulator = docAccumulatorMap.get(docId);
            if (accumulator != 0.0) {
                double docWeight = getDoucumentWeight(docId);
                docAccumulatorMap.put(docId, accumulator / docWeight);
            }

        }

        PriorityQueue<Integer> priorityQueue = new PriorityQueue<>(10,
                (w1, w2) -> docAccumulatorMap.get(w1).compareTo(docAccumulatorMap.get(w2)));

        for (int docId : docAccumulatorMap.keySet()) {
            // if(docAccumulatorMap.get(docId)>1)
            // System.out.println(corpus.getDocument(docId).getName()+" acc:"+docAccumulatorMap.get(docId));
            priorityQueue.offer(docId);
            if (priorityQueue.size() > 10) priorityQueue.poll();
        }

        ArrayList<Integer> rankedRetrievalPosting = new ArrayList<>();
        while (!priorityQueue.isEmpty()) {
            rankedRetrievalPosting.add(priorityQueue.poll());
        }
        Collections.reverse(rankedRetrievalPosting);

        for (Integer docId : rankedRetrievalPosting) {
            Document document = corpus.getDocument(docId);
            document.getContent();
            System.out.println("Title \"" + document.getTitle() + "\" File Name: " + corpus.getDocument(docId).getName() + " : " + docAccumulatorMap.get(docId));
        }
        return rankedRetrievalPosting;
    }

    /*
     * Using document Id and docweight.bin fetch the document weight
     * */
    public double getDoucumentWeight(int docId) {

        double ld = -1;
        try {
            randomAccessDocWeight.seek(docId * 8);
            ld = randomAccessDocWeight.readDouble();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ld;
    }

//    public static void main(String[] args){
//        BetterTokenProcessor processor = new BetterTokenProcessor();
//        DocumentCorpus corpus = DirectoryCorpus.loadJsonDirectory(Paths.get("C:\\Articles_full\\Articles").toAbsolutePath(), ".json");
//        corpus.getDocuments();
//        Index index =   new DiskPositionalIndex(Paths.get(Paths.get("C:\\Articles_full\\Articles").toAbsolutePath() + "\\index"));
//        RankedRetrieval rankedRetrieval = new RankedRetrieval(Paths.get("C:\\Articles_full\\Articles\\index").toAbsolutePath());
//
//        List<Integer> postings = rankedRetrieval.doRankedRetrieval("crater lake", corpus, index, processor);
//    }

}
