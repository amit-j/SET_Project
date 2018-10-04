package edu.csulb;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.index.Index;
import cecs429.index.PositionalInvertedIndex;
import cecs429.index.Posting;
import cecs429.index.wildcard.KGramIndex;
import cecs429.query.BooleanQueryParser;
import cecs429.query.QueryComponent;
import cecs429.text.BetterTokenProcessor;
import cecs429.text.EnglishTokenStream;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;


public class InvertedIndexer {


    public static void main(String[] args) {

        DocumentCorpus corpus = null;
        Index index = null;
        QueryComponent component;
        BooleanQueryParser parser = new BooleanQueryParser();
        KGramIndex wildcardIndexer = null;

        BetterTokenProcessor processor = new BetterTokenProcessor();
        // We aren't ready to use a full query parser; for now, we'll only support single-term queries.

        SnowballStemmer snowballStemmer = new englishStemmer();
        String query = "whale"; // hard-coded search for "whale"

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Name of the directory to index: ");
        String directoryPath = null;
        File file = null;
        try {
            directoryPath = reader.readLine();
            file = new File(directoryPath);
            if (file.isDirectory() && corpus == null) {
                corpus = DirectoryCorpus.loadJsonDirectory(Paths.get(directoryPath).toAbsolutePath(), ".json");
                index = indexCorpus(corpus);
                wildcardIndexer = new KGramIndex(index);
            } else if (!file.isDirectory()) {
                System.out.println("Enter valid directory path");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (!query.equalsIgnoreCase("q")) {

            try {

                System.out.print("Enter your query: ");
                query = reader.readLine();
                String[] queryLiterals = query.split(" ");

                switch (queryLiterals[0]) {
                    case "q":
                        break;
                    case "stem":
                        snowballStemmer.setCurrent(queryLiterals[1]);
                        snowballStemmer.stem();
                        String stemmedToken = snowballStemmer.getCurrent();
                        System.out.println("The stemmed term: " + stemmedToken);
                        break;
                    case "index":
                        if (file.isDirectory()) {
                            corpus = DirectoryCorpus.loadJsonDirectory(Paths.get(queryLiterals[1]).toAbsolutePath(), ".json");
                            wildcardIndexer = new KGramIndex(index);

                            index = indexCorpus(corpus);

                        } else {
                            System.out.println("Enter valid directory path");
                        }
                        break;
                    case "vocab":
                        List<String> sortedVocabulary = index.getVocabulary().stream().sorted().collect(Collectors.toList());
                        ;
                        int i = 0;
                        for (String vocab : sortedVocabulary) {
                            System.out.println(vocab);
                            i++;
                            if (i == 999) break;
                        }
                        break;
                    default:
                        System.out.println("Your query: " + query);
                        component = parser.parseQuery(query, wildcardIndexer);
                        for (Posting p : component.getPostings(index)) {
                            System.out.println("Json Document " + corpus.getDocument(p.getDocumentId()).getName());
                        }
                        System.out.println("Total number of documents returned from the query: " +
                                " " + component.getPostings(index).size());
                        System.out.print("Would you like to select a document name to view? (Y/N)");
                        String wantToView = reader.readLine();
                        if (wantToView.equalsIgnoreCase("y")) {
                            System.out.print("Enter the document name: ");
                            String documentName = reader.readLine();
                            for (Document document : corpus.getDocuments()) {
                                if (document.getName().equalsIgnoreCase(documentName.trim())) {
                                    Scanner scanner = new Scanner(document.getContent()).useDelimiter("\\A");
                                    String str = scanner.hasNext() ? scanner.next() : "";
                                    System.out.println(str);
                                    break;
                                }
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

        System.out.println("started reading document:");
        long start = System.currentTimeMillis();
        int documentCount = 0;
        for (Document document : corpus.getDocuments()) {
//            if(documentCount>10){
//                break;
//            }
            documentCount++;

            EnglishTokenStream tokenStream = new EnglishTokenStream(document.getContent());
            //System.out.println("reading document: "+document.getTitle());

            int position = 0;
            for (String token : tokenStream.getTokens()) {
                for (String term : processor.processToken(token)) {
                    index.addTerm(term, document.getId(), position++);


                }
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("Indexing process took: " + ((end - start) / 1000) + " seconds");
        return index;
    }


}
