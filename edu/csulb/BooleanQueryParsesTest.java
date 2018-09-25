package edu.csulb;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.DocumentCorpus;
import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.query.BooleanQueryParser;
import cecs429.query.QueryComponent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;

public class BooleanQueryParsesTest {

    public static void main(String[] args){

        String query = "whale"; // hard-coded search for "whale"
        BooleanQueryParser parser = new BooleanQueryParser();
        QueryComponent component ;
        while (!query.equalsIgnoreCase("quit")){
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Enter your query: ");


            try {
                query = reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //System.out.println("Your query is: " + query);

           component=  parser.parseQuery(query);



        }
}
}

