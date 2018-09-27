package edu.csulb;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.index.*;
import cecs429.text.BasicTokenProcessor;
import cecs429.text.BetterTokenProcessor;
import cecs429.text.EnglishTokenStream;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;


public class InvertedIndexer {


    public static void main(String[] args){

        DocumentCorpus corpus = null;
        Index index = null;
    BetterTokenProcessor processor = new BetterTokenProcessor();
    // We aren't ready to use a full query parser; for now, we'll only support single-term queries.

        SnowballStemmer snowballStemmer = new englishStemmer();
        String query = "whale"; // hard-coded search for "whale"
        while (!query.equalsIgnoreCase("q")){
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Name of the directory to index: ");
        try {
            query = reader.readLine();
            String[] queryLiterals = query.split(" ");
            System.out.println(queryLiterals[0]);

            File file = new File(queryLiterals[1]);
            if(file.isDirectory() && corpus == null){
                corpus = DirectoryCorpus.loadJsonDirectory(Paths.get(queryLiterals[1]).toAbsolutePath(), ".json");
                index = indexCorpus(corpus) ;

            } else {
                System.out.println("Enter valid directory path");
            }

            switch (queryLiterals[0]) {
                case "stem":
                    snowballStemmer.setCurrent(queryLiterals[1]);
                    snowballStemmer.stem();
                    String stemmedToken = snowballStemmer.getCurrent();
                    System.out.println(stemmedToken);
                    break;
                case "index":
                    if(file.isDirectory()){
                        corpus = DirectoryCorpus.loadJsonDirectory(Paths.get(queryLiterals[1]).toAbsolutePath(), ".json");
                        index = indexCorpus(corpus) ;

                    } else {
                        System.out.println("Enter valid directory path");
                    }
                    break;
                case "vocab":
                    List<String> vocabulary = index.getVocabulary();
                    int i = 0;
                    for (String vocab : vocabulary){
                        System.out.print(vocab);
                        i++;
                        if(i==999) break;
                    }
                    break;
                default:
                    for(String term : processor.processToken(query)) {

                        for (Posting p : index.getPostings(term)) {
                            System.out.println("Json Document " + corpus.getDocument(p.getDocumentId()).getTitle());
                        }
                    }
                    break;
            }
            } catch (IOException e) {
            e.printStackTrace();
        }

    }


}

    private static Index indexCorpus(DocumentCorpus corpus) {
        BetterTokenProcessor processor = new BetterTokenProcessor();

        PositionalInvertedIndex index = new PositionalInvertedIndex();


        int documentCount = 0;
        for(Document document:corpus.getDocuments()){
            if(documentCount>300){
                break;
            }
            documentCount++;

            EnglishTokenStream tokenStream = new EnglishTokenStream(document.getContent());
            System.out.println("reading document: "+document.getTitle());

            int position = 0;
            for(String token:tokenStream.getTokens()){
                for(String term : processor.processToken(token)) {
                    index.addTerm(term, document.getId(), position++);
                }
            }



        }

        return index;
    }


}
