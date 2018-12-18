package edu.csulb;

import cecs429.clustering.ClusterPruningIndex;
import cecs429.documents.DocumentCorpus;
import cecs429.index.Index;
import cecs429.index.RankedRetrieval;
import cecs429.text.TokenProcessor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

public class Milestone3 {

    Map<String, Set<Integer>> queries;
    String corpusPath;

    public Milestone3(String corpusPath) {
        this.corpusPath = corpusPath;
    }

    Map<String, Set<Integer>> readQueryDocs() {

        BufferedReader queryReader;
        BufferedReader relevanceReader;

        queries = new HashMap<>();
        try {
            queryReader = new BufferedReader(new FileReader(corpusPath + "\\relevance\\queries"));
            relevanceReader = new BufferedReader(new FileReader(corpusPath + "\\relevance\\qrel"));

            String line = queryReader.readLine();

            while (line != null) {


                String relevanceString = relevanceReader.readLine();
                String[] relevance = relevanceString.split("\\s+");
                Set<Integer> list = new HashSet<>();
                for (String temp : relevance) {
                    list.add(Integer.parseInt(temp));
                }

                queries.put(line, list);
                // read next line
                line = queryReader.readLine();
            }
            queryReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return queries;
    }


    public void compareMAP(DocumentCorpus corpus, Index index, TokenProcessor processor, ClusterPruningIndex clusterIndex) {
        RankedRetrieval rankedRetrieval = new RankedRetrieval(Paths.get(corpusPath + "\\index").toAbsolutePath());


        Map<String, Set<Integer>> queryRelevance = readQueryDocs();
        double totalPrecision = 0;

        double meanAveragePrecision = 0;

//        for (String query : queryRelevance.keySet()) {
//
//            List<Integer> retrievedDocs = rankedRetrieval.doRankedRetrieval(query, corpus, index, processor);
//            meanAveragePrecision += calculateMAP(retrievedDocs, queryRelevance, query, corpus);
//
//
//        }
//
//
//        System.out.println("********************Mean Average Precision " + meanAveragePrecision / (queryRelevance.size()));
//
//        System.out.println("");
//        System.out.println("----------------------Ranked retrival using clusers-------------------------------");
//
//        meanAveragePrecision = 0;
//        for (String query : queryRelevance.keySet()) {
//
//
//            List<Integer> retrievedDocs = clusterIndex.getPostings(processor.processToken(query));
//            meanAveragePrecision += calculateMAP(retrievedDocs, queryRelevance, query, corpus);
//
//
//        }
//
//
//        System.out.println("********************Mean Average Precision " + meanAveragePrecision / (queryRelevance.size()));


        System.out.println("Through put for Ranked Retrival index");
        long start = System.currentTimeMillis();
        for (String query : queryRelevance.keySet()) {
            rankedRetrieval.doRankedRetrieval(query, corpus, index, processor);

        }
        long end = System.currentTimeMillis();
        double throughput = queryRelevance.keySet().size() / ((double) (end - start) / 1000);
        System.out.println(" " + throughput + " seconds");
        System.out.println("Mean response time "+ (1/throughput)*1000+"ms");

        System.out.println("Through put for cluster index");
        start = System.currentTimeMillis();
        for (String query : queryRelevance.keySet()) {
            clusterIndex.getPostings(processor.processToken(query));

        }
        end = System.currentTimeMillis();
        throughput = queryRelevance.keySet().size() / ((double) (end - start) / 1000);
        System.out.println(" " + throughput + " q/secs");
        System.out.println("Mean response time "+ (1/throughput)*1000+"ms");



    }


    public double calculateMAP(List<Integer> retrievedDocs, Map<String, Set<Integer>> queryRelevance, String query, DocumentCorpus corpus) {


        double meanAveragePrecision = 0;

        double numberOfRelevantDocs = 0;
        double precisionAt = 0;
        double precision = 0;

        Set<Integer> relevantDocs = queryRelevance.get(query);

        for (int doc : retrievedDocs) {
            precisionAt++;
            if (relevantDocs.contains(Integer.parseInt(corpus.getDocument(doc).getName().replace(".json", "")))) {
                numberOfRelevantDocs++;
                precision += numberOfRelevantDocs / precisionAt;
                System.out.println("Relevant doc Name " + corpus.getDocument(doc).getName() + "At index " + precisionAt);
            }
        }

        double averagePrecision = precision / relevantDocs.size();
        meanAveragePrecision += averagePrecision;
        //calculate precision
        System.out.println("**********Query :" + query + " Average Precision " + averagePrecision);
        System.out.println("");
        return meanAveragePrecision;


    }
}

