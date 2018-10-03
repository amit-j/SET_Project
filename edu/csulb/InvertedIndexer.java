package edu.csulb;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.index.*;
import cecs429.query.BooleanQueryParser;
import cecs429.query.QueryComponent;
import cecs429.text.BasicTokenProcessor;
import cecs429.text.EnglishTokenStream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.HashSet;

public class InvertedIndexer {


    public static void main(String[] args){
    DocumentCorpus corpus = DirectoryCorpus.loadJsonDirectory(Paths.get("C://Articles").toAbsolutePath(), ".json");
    WildcardIndexer wildcardIndexer = new WildcardIndexer(new PositionalInvertedIndex());
    Index index = indexCorpus(corpus,wildcardIndexer) ;
    // We aren't ready to use a full query parser; for now, we'll only support single-term queries.
    String query = "whale"; // hard-coded search for "whale"
         QueryComponent component ;
        BooleanQueryParser parser = new BooleanQueryParser();
        while (!query.equalsIgnoreCase("quit")){
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Enter your query: ");
        try {
            query = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println("Your query is: " + query);

             component = parser.parseQuery(query,wildcardIndexer.getIndex());
                for (Posting p : component.getPostings(index)) {
                    System.out.println("Json Document " + corpus.getDocument(p.getDocumentId()).getName());
                }

             component = parser.parseQuery(query,wildcardIndexer.getIndex());
        System.out.println(component.getPostings(index).size());
        for (Posting p : component.getPostings(index)) {
            //System.out.println("Json Document " + corpus.getDocument(p.getDocumentId()).getTitle());
        }


    }


}

    private static Index indexCorpus(DocumentCorpus corpus,WildcardIndexer wildcardIndexer) {
        BasicTokenProcessor processor = new BasicTokenProcessor();

        PositionalInvertedIndex index = new PositionalInvertedIndex();


        int documentCount = 0;
        for(Document document:corpus.getDocuments()){

            if(documentCount>300){
                break;
            }


            documentCount++;

            EnglishTokenStream tokenStream = new EnglishTokenStream(document.getContent());
            //System.out.println("reading document: "+document.getTitle());

            int position = 0;
            for(String token:tokenStream.getTokens()){

                String processedToken = processor.processToken(token);
                index.addTerm(processedToken,document.getId(), position);
                wildcardIndexer.addTerm(processedToken,document.getId(),position++);
            }



        }

        return index;
    }


}
