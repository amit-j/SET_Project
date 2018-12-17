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

import javax.print.Doc;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

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

        for(int i =0;i<indexList.size();i++){
             BayesianClass bayesianClass = new BayesianClass(vocabSet.size(),indexList.get(i),corpusSize.get(i),totalDocuments,totalClassCount.get(i),className.get(i));
            bayesianClasses.add(bayesianClass);
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

}
