package cecs429.classifier;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.index.Index;
import cecs429.index.PositionalInvertedIndex;
import cecs429.text.BetterTokenProcessor;
import cecs429.text.EnglishTokenStream;
import cecs429.text.TokenProcessor;
import javafx.geometry.Pos;
import org.omg.PortableInterceptor.NON_EXISTENT;

import javax.print.Doc;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ClassifyDocuemnts {

    public static void main(String[] args){

         RochioClassifier classifier;
         TokenProcessor processor = new BetterTokenProcessor();
         List<RochioClass> classList = new ArrayList<>();
         List<Index> indexList = new ArrayList<>();
         List<Integer> totalClassCount = new ArrayList<>();
         List<Integer> corpusSize = new ArrayList<>();
         HashSet<String> vocabSet = new HashSet<>();
         List<String> className = new ArrayList<>();

         List<BayesianClass> bayesianClasses = new ArrayList<>();
            int totalDocuments = 0;
         for(int i=0;i<args.length-1;i++){

            DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(Paths.get(args[i]).toAbsolutePath(), ".txt");
            PositionalInvertedIndex index = new PositionalInvertedIndex();

            File file = new File(args[i]);
            int termCount = 0;
            for (Document document : corpus.getDocuments()) {


                EnglishTokenStream tokenStream = new EnglishTokenStream(document.getContent());

                int position = 0;

                for (String token : tokenStream.getTokens()) {

                    for (String term : processor.processToken(token)) {
                        if(!term.equals("")) {
                            termCount++;
                            index.addTerm(term, document.getId(), position++);
                        }

                    }

                }

            }

            indexList.add(index);
            totalClassCount.add(termCount);
            vocabSet.addAll(index.getVocabulary());

             corpusSize.add(corpus.getCorpusSize());
             totalDocuments+=corpus.getCorpusSize();
            RochioClass rochioClass = new RochioClass(corpus,new BetterTokenProcessor(),file.getName(),index);
             file = new File(args[i]);
            className.add(file.getName());
            classList.add(rochioClass);

        }

        DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(Paths.get(args[args.length-1]).toAbsolutePath(), ".txt");


        List<HashMap<String,Double>> discriminatingTerms = new ArrayList<>();



        for(int i =0;i<indexList.size();i++){
             BayesianClass bayesianClass = new BayesianClass(vocabSet.size(),indexList.get(i),corpusSize.get(i),totalDocuments,totalClassCount.get(i),className.get(i));
            bayesianClasses.add(bayesianClass);
         }


        //calculate discriminating terms
        for(int iCurr = 0;iCurr<indexList.size();iCurr++){
            HashMap<String,Double> dterms = new HashMap<>();
            PriorityQueue<TermICTMap> priorityQueue = new PriorityQueue<>(50,(w1,w2)-> Double.compare(w1.ict,w2.ict));


            int termid =0;

            for(String term:indexList.get(iCurr).getVocabulary()){

                int NOneOne = indexList.get(iCurr).getPostings(term).size();
                int NZeroOne  = bayesianClasses.get(iCurr).getClassCorpusSize() - NOneOne;
                int NOneZero = 0;
                int NZeroZero =0;
                for(int jCurr = 0; jCurr< indexList.size();jCurr++){
                    if(iCurr==jCurr)
                        continue;
                    else{
                        NOneZero+= indexList.get(jCurr).getPostings(term).size();
                        NZeroZero += bayesianClasses.get(jCurr).getClassCorpusSize();
                    }
                }

                NZeroZero -=NOneZero;


                double ict = calculateICT(NOneOne, NOneZero,NZeroOne,NZeroZero);
                TermICTMap map = new TermICTMap();
                map.ict = ict;
                map.termID = termid;
                priorityQueue.offer(map);
                if(priorityQueue.size()>50) priorityQueue.poll();
                termid++;

            }

            bayesianClasses.get(iCurr).setDiscriminatingTerms(priorityQueue);


        }



        for(Document doc:corpus.getDocuments()) {
        double max = -999999999;
         String maxClass ="";
             for (BayesianClass bayesianClass : bayesianClasses) {
                 double prob = bayesianClass.calculateProbability(doc,processor);
                 if(prob>max){
                     max = prob;
                     maxClass = bayesianClass.getClassName();
                 }
             }

             System.out.println("Document "+doc.getName()+" classified in class :"+maxClass+" using Bayesian classifier");
         }

        classifier = new RochioClassifier(classList,corpus);
        classifier.trainClassifier(processor);




    }

    private static double calculateICT(int NOneOne,int NOneZero,int NZeroOne,int NZeroZero){
        double N = NOneOne+ NOneZero+NZeroOne+NZeroZero;

        double term1 = (NOneOne/N) * logBase2((N*NOneOne)/((NOneZero+NOneOne) * (NZeroOne+NOneOne)));
        double term2 = (NOneZero/N)*logBase2((N*NOneZero)/((NZeroOne+NZeroZero)*(NOneZero+NZeroZero)));
        double term3 = (NZeroOne/N)*logBase2((N*NZeroOne)/((NZeroOne+NZeroZero)*(NOneOne+NOneZero)));
        double term4 = (NZeroZero/N)*logBase2((N*NZeroZero)/((NZeroOne+NZeroZero)*(NZeroZero+NOneZero)));


        return term1+term2+term3+term4;


    }


    private static double logBase2(double x){

        if(x==0|| Double.isInfinite(x) || Double.isNaN(x)){
            return 0;
        }

        double log = Math.log(x);

        if(log == 0 || log == Double.NEGATIVE_INFINITY || log== Double.POSITIVE_INFINITY || log == Double.NaN){
            return 0;
        }
        return log / Math.log(2);
    }



}


class TermICTMap{

    int termID;
    double ict;
}