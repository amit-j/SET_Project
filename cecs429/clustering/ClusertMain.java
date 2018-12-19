package cecs429.clustering;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.index.PositionalInvertedIndex;
import cecs429.text.BetterTokenProcessor;
import cecs429.text.EnglishTokenStream;
import cecs429.text.TokenProcessor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class ClusertMain {


    public static void main(String[] args) throws IOException {
        PositionalInvertedIndex index = new PositionalInvertedIndex();

        DocumentCorpus corpus = DirectoryCorpus.loadJsonDirectory(Paths.get(args[0]).toAbsolutePath(), ".json");
        int termCount = 0;
        TokenProcessor processor = new BetterTokenProcessor();


        for (Document document : corpus.getDocuments()) {


            EnglishTokenStream tokenStream = new EnglishTokenStream(document.getContent());

            int position = 0;

            for (String token : tokenStream.getTokens()) {

                for (String term : processor.processToken(token)) {
                    if (!term.equals("")) {
                        termCount++;
                        index.addTerm(term, document.getId(), position++);
                    }

                }

            }

        }
        File file = new File("C:\\Rel");
        ClusterPruningIndex clusterPruningIndex = new ClusterPruningIndex();
        //  clusterPruningIndex.buildIndex(corpus,index);
        //clusterPruningIndex.writeToDisk(Paths.get(file.toPath().toAbsolutePath() + ""));
        clusterPruningIndex.readFromDisk(Paths.get(file.toPath().toAbsolutePath() + ""), index);
        String term[] = {"nation", "park", "in", "california"};
        for (Integer p : clusterPruningIndex.getPostings(term)) {
            System.out.println("Document found :" + corpus.getDocument(p).getName());
        }


    }
}
