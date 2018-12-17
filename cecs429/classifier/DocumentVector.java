package cecs429.classifier;

import cecs429.documents.Document;

import java.util.HashMap;
import java.util.List;

public class DocumentVector {
        //TODO: remove vocab and docid

    private HashMap<Integer,Double> docVector;
    private List<String> classVocab ;
    private int documentID;

    public DocumentVector (List<String> _classVocab,int documentID){
        this.classVocab = _classVocab;
        docVector  = new HashMap<>();
        this.documentID = documentID;
    }

    public void addWdt(double wdt,int termID){
        if(docVector.containsKey(termID)){
            double w = docVector.get(termID);
            w+=wdt;

        }
        else
        docVector.put(termID,wdt);
    }

    public HashMap<Integer,Double> getVector(){return  docVector;}
    public void addVector(DocumentVector vector,int numDocuments,double ld){
        double normalize = (ld*numDocuments);
        for(Integer termID:vector.getVector().keySet()){

            if(docVector.containsKey(termID)){
                docVector.put(termID,(vector.getVector().get(termID))/normalize+ docVector.get(termID));
            }
            else{
                docVector.put(termID,vector.getVector().get(termID)/normalize);
            }
        }
    }



    public Double calculateDistance(DocumentVector v2){

        double distance = 0;
        for(Integer termID:docVector.keySet()){
          //  distance+= Math.pow(docVector.get(termID) - (v2.getVector().containsKey(termID)?v2.getVector().get(termID):0),2);

            double vec1 = docVector.get(termID);
            double vec2 = v2.getVector().containsKey(termID)?v2.getVector().get(termID):0;

            double diff = vec1 - vec2;

            distance += diff*diff;
            //distance+=( docVector.get(termID) - (v2.getVector().containsKey(termID)?v2.getVector().get(termID):0)) * (docVector.get(termID) - (v2.getVector().containsKey(termID)?v2.getVector().get(termID):0));
        }
        return Math.sqrt(distance);
    }

}
