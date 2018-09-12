package edu.csulb;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.index.TermDocumentIndex;
import cecs429.text.BasicTokenProcessor;
import cecs429.text.EnglishTokenStream;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


public class BetterTermDocumentIndexer {

    Gson gson;


	public static void main(String[] args) {

		DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(Paths.get("C://Articles/").toAbsolutePath(), ".json");
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
		HashSet<String> vocabulary = new HashSet<>();
		BasicTokenProcessor processor = new BasicTokenProcessor();
		
		// First, build the vocabulary hash set.
		
		// TODO:
		// Get all the jsonDocuments in the corpus by calling GetDocuments().
		// Iterate through the jsonDocuments, and:
		// Tokenize the document's content by constructing an EnglishTokenStream around the document's content.
		// Iterate through the tokens in the document, processing them using a BasicTokenProcessor,
		//		and adding them to the HashSet vocabulary.



        for(Document document:corpus.getDocuments()){

            EnglishTokenStream tokenStream = new EnglishTokenStream(document.getContent());

            for(String token:tokenStream.getTokens()){

                vocabulary.add(processor.processToken(token));
            }



        }
		
		// TODO:
		// Constuct a TermDocumentMatrix once you know the size of the vocabulary.
		// THEN, do the loop again! But instead of inserting into the HashSet, add terms to the index with addPosting.

        TermDocumentIndex index = new TermDocumentIndex(vocabulary,corpus.getCorpusSize());

        for(Document document:corpus.getDocuments()){

            EnglishTokenStream tokenStream = new EnglishTokenStream(document.getContent());

            for(String token:tokenStream.getTokens()){

               index.addTerm(processor.processToken(token),document.getId());
            }



        }

		return index;
	}



}
