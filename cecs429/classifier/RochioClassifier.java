package cecs429.classifier;

import cecs429.clustering.DocumentVector;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.index.PositionalInvertedIndex;
import cecs429.text.EnglishTokenStream;
import cecs429.text.TokenProcessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RochioClassifier {

    private List<RochioClass> classes;
    private DocumentCorpus testSet;
    private List<DocumentVector> centroids;

    private PositionalInvertedIndex unclassifiedIndex;


    public RochioClassifier(List<RochioClass> classes, DocumentCorpus unclassified) {
        this.classes = classes;
        testSet = unclassified;

    }

    public void trainClassifier(TokenProcessor processor) {
        centroids = new ArrayList<>();

        for (RochioClass rochioClass : classes) {
            rochioClass.calculateCentroid();
            centroids.add(rochioClass.getCentroid());

        }


        for (Document document : testSet.getDocuments()) {


            EnglishTokenStream tokenStream = new EnglishTokenStream(document.getContent());
            HashMap<String, Integer> documentTermFreq = new HashMap<>();
            int position = 0;

            for (String token : tokenStream.getTokens()) {

                for (String term : processor.processToken(token)) {
                    if (!term.equals(""))

                    {
                        if (documentTermFreq.containsKey(term)) {
                            documentTermFreq.put(term, documentTermFreq.get(term) + 1);
                        } else {
                            documentTermFreq.put(term, 1);
                        }
                    }

                }

            }
            double ld = 0;
            HashMap<String, Double> docWeights = new HashMap<>();
            for (String term : documentTermFreq.keySet()) {
                double wdt = 1 + Math.log(documentTermFreq.get(term));
                ld += wdt * wdt;
                docWeights.put(term, wdt);

            }


            ld = Math.sqrt(ld);

            System.out.printf("ld" + ld + "for doc:" + document.getName());
            double min = Double.MAX_VALUE;
            RochioClass minClass = null;
            for (RochioClass rochioClass : classes) {

                double dist = rochioClass.calculateClassDistance(document, docWeights, ld);
                if (dist < min) {
                    min = dist;
                    minClass = rochioClass;
                }
            }

            System.out.println("Document " + document.getName() + " classified as " + minClass.getName() + " with a dist of " + min);


        }
    }

//    public void ClassifyDocuemnts(){
//        for(Document doc:testSet.getDocuments()){
//            double min=Double.MAX_VALUE;
//            RochioClass minClass = null;
//            double dist=0;
//            for(RochioClass rochioClass:classes) {
//                dist = rochioClass.calculateClassDistance(doc, unclassifiedIndex);
//                if (dist < min) {
//                    min = dist;
//                    minClass = rochioClass;
//                }
//            }
//
//
//            System.out.println("Document class for document "+doc.getTitle() +" is "+minClass.getName());
//        }
//    }


}
