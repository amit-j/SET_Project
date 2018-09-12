package edu.csulb;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.index.Index;
import cecs429.index.InvertedIndex;
import cecs429.index.Posting;
import cecs429.index.TermDocumentIndex;
import cecs429.text.BasicTokenProcessor;
import cecs429.text.EnglishTokenStream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.HashSet;

public class InvertedIndexer {


    public static void main(String[] args){
    DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(Paths.get("C://Articles_2/").toAbsolutePath(), ".txt");
    Index index = indexCorpus(corpus) ;
    // We aren't ready to use a full query parser; for now, we'll only support single-term queries.
    String query = "whale"; // hard-coded search for "whale"

        while (!query.equalsIgnoreCase("quit")){
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Enter your query: ");


        try {
            query = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println("Your query is: " + query);


        for (Posting p : index.getPostings(query)) {
            System.out.println("JsonDocument " + corpus.getDocument(p.getDocumentId()).getTitle());
        }

    }


}

    private static Index indexCorpus(DocumentCorpus corpus) {
        BasicTokenProcessor processor = new BasicTokenProcessor();

        InvertedIndex index = new InvertedIndex();

        for(Document document:corpus.getDocuments()){

            EnglishTokenStream tokenStream = new EnglishTokenStream(document.getContent());

            for(String token:tokenStream.getTokens()){

                index.addTerm(processor.processToken(token),document.getId());
            }



        }

        return index;
    }


}
