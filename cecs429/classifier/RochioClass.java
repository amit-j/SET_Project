package cecs429.classifier;

import cecs429.clustering.DocumentVector;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.index.PositionalInvertedIndex;
import cecs429.index.Posting;
import cecs429.text.TokenProcessor;

import java.util.HashMap;
import java.util.List;

public class RochioClass {

    private List<String> vocabList;
    private HashMap<String, Integer> vocabSet;
    private DocumentCorpus corpus;
    private HashMap<Integer, DocumentVector> documentVectors;
    private PositionalInvertedIndex index;
    private HashMap<Integer, Double> docuementWeights;
    private String className;
    private DocumentVector centroid;

    public RochioClass(DocumentCorpus _corpus, TokenProcessor processor, String _className, PositionalInvertedIndex _index) {

        this.corpus = _corpus;
        index = _index;
        documentVectors = new HashMap<Integer, DocumentVector>();
        docuementWeights = new HashMap<>();
        vocabSet = new HashMap<>();
        className = _className;


        this.vocabList = index.getVocabulary();


    }


    public DocumentVector getCentroid() {
        return centroid;
    }

    public void calculateCentroid() {

        int termID = 0;
        for (String term : vocabList) {

            for (Posting posting : index.getPostingsWithPositions(term)) {

                double wdt = 1 + Math.log(posting.getPositions().size());
                int docId = posting.getDocumentId();

                if (documentVectors.containsKey(docId)) {
                    DocumentVector vector = documentVectors.get(docId);
                    vector.addWdt(wdt, termID);
                } else {
                    DocumentVector vector = new DocumentVector(docId);
                    vector.addWdt(wdt, termID);
                    documentVectors.put(docId, vector);

                }

                if (docuementWeights.containsKey(docId)) {
                    Double weight = docuementWeights.get(docId);
                    weight += wdt * wdt;
                    docuementWeights.put(docId, weight);
                } else {

                    Double weight = new Double(0);
                    weight += wdt * wdt;
                    docuementWeights.put(docId, weight);
                }


            }
            vocabSet.put(term, termID);
            termID++;
        }


        for (Integer docID : docuementWeights.keySet()) {
            Double weight = docuementWeights.get(docID);
            Double ld = Math.sqrt(weight);
            docuementWeights.put(docID, ld);

        }

        centroid = new DocumentVector(-1); //centroid wont have any document ID
        for (Document document : corpus.getDocuments()) {

            centroid.addVector(documentVectors.get(document.getId()), corpus.getCorpusSize(), docuementWeights.get(document.getId()));

        }


    }

    public Double calculateClassDistance(Document doc, HashMap<String, Double> docWeights, Double ld) {

        return getCentroid().calculateDistance(createVectorForClass(doc, docWeights, ld));
    }

    private DocumentVector createVectorForClass(Document document, HashMap<String, Double> docWeights, Double ld) {

        DocumentVector documentVector = new DocumentVector(-1);
        int termID = 0;
        for (String term : vocabList) {
            if (docWeights.containsKey(term)) {
                documentVector.addWdt(docWeights.get(term) / ld, termID);
            }

            termID++;
        }


        return documentVector;
    }

    public String getName() {
        return className;
    }
}


