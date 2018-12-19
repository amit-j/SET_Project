package cecs429.classifier;

import cecs429.documents.Document;
import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.EnglishTokenStream;
import cecs429.text.TokenProcessor;

import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

public class BayesianClass {

    private double classProbability;
    private int totalClassTerms;
    private int totalVocabCount;
    private int classVocabSize;
    private Index index;
    private String className;
    private HashMap<String, Double> discriminatingTerms;
    private int classCorpusSize;
    private int totalDocuments;

    public BayesianClass(int totalVocabCount, Index index, int corpusSize, int totalDocuemnts, int totalClassVocabCount, String name) {

        classCorpusSize = corpusSize;

        this.totalDocuments = totalDocuemnts;
        double prob = (double) corpusSize / totalDocuemnts;
        classProbability = Math.log10(prob);
        totalClassTerms = totalClassVocabCount;
        this.totalVocabCount = totalVocabCount;
        classVocabSize = index.getVocabulary().size();
        className = name;
        this.index = index;

    }

    public int getClassCorpusSize() {
        return classCorpusSize;
    }

    public int getTotalDocuments() {
        return totalDocuments;
    }

    public double calculateProbability(Document document, TokenProcessor processor) {

        EnglishTokenStream tokenStream = new EnglishTokenStream(document.getContent());

        int position = 0;

        double totalProb = 0;
        for (String token : tokenStream.getTokens()) {

            for (String term : processor.processToken(token)) {
                int freq = 0;
                if (!term.equals("") && discriminatingTerms.containsKey(term)) {
                    List<Posting> postingList = index.getPostingsWithPositions(term);
                    for (Posting p : postingList) {
                        freq += p.getPositions().size();
                    }


                    double denom = totalClassTerms + totalVocabCount;
                    double num = 1 + freq;
                    totalProb += Math.log10(num / denom);
                    //  totalProb += Math.log((1 + freq) /() (totalClassTerms + totalVocabCount));
                }

            }

        }

        totalProb += classProbability;

        return totalProb;
    }


    public void setDiscriminatingTerms(PriorityQueue<TermICTMap> ictMaps) {
        discriminatingTerms = new HashMap<>();
        System.out.println("Discriminating terms for class " + className + ":");
        for (TermICTMap map : ictMaps) {
            discriminatingTerms.put(index.getVocabulary().get(map.termID), map.ict);
            System.out.println(index.getVocabulary().get(map.termID));
        }
    }

    public String getClassName() {
        return className;
    }
}
