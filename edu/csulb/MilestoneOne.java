package edu.csulb;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.index.*;
import cecs429.index.IndexWriter.SinglePassInMemoryIndexWriter;
import cecs429.index.wildcard.KGramIndex;
import cecs429.query.BooleanQueryParser;
import cecs429.query.QueryComponent;
import cecs429.text.BetterTokenProcessor;
import cecs429.text.EnglishTokenStream;
import cecs429.text.TokenProcessor;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;


public class MilestoneOne {

    public static void main(String[] args) throws IOException {

        DocumentCorpus corpus = null;
        Index index = null;
        QueryComponent component;
        BooleanQueryParser parser = new BooleanQueryParser();
        KGramIndex wildcardIndexer = null;
        TokenProcessor tokenProcessor = null;
        RankedRetrieval rankedRetrieval = null;
        String queryMode = "";
        SnowballStemmer snowballStemmer = new englishStemmer();
        String query = "whale"; // hard-coded search for "whale"

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));


        String mileStoneChoice = "";
        System.out.println("Please select a milestone to start ?");
        System.out.println("1. Milestone 1 (In memory Inverted Positional Index, K-Grams)");
        System.out.println("2. Milestone 2 (OnDisk Indexing, B+ Tree on disk vocabulary)");
        System.out.println("3. Quit");
        System.out.print(">> ");
        mileStoneChoice = reader.readLine();
        switch (mileStoneChoice) {
            case "1":
                System.out.print("Enter the corpus path : ");
                String corpusPath = reader.readLine();
                corpus = DirectoryCorpus.loadTextDirectory(Paths.get(corpusPath).toAbsolutePath(), ".txt");
                corpus.getDocuments();
                TokenProcessor processor = new BetterTokenProcessor();
                index = indexCorpus(corpus, processor);
                wildcardIndexer = new KGramIndex(index, getUnstemmedVocabs(corpusPath), tokenProcessor);
                queryMode = "1";
                break;
            case "2":


                String choice = "";
                try {
                    System.out.println("What would you like to do ?");
                    System.out.println("1. Build & Query Index");
                    System.out.println("2. Query Index");
                    System.out.println("3. Quit");
                    System.out.print(">> ");

                    choice = reader.readLine();
                    corpusPath = null;
                    switch (choice) {


                        case "1":
                            File file = null;
                            //build index
                            while (file == null) {

                                System.out.print("Enter the corpus path : ");
                                corpusPath = reader.readLine();
                                file = new File(corpusPath);
                                if (file.isDirectory() && corpus == null) {

                                    corpus = DirectoryCorpus.loadTextDirectory(Paths.get(corpusPath).toAbsolutePath(), ".txt");
                                    SinglePassInMemoryIndexWriter indexWriter = new SinglePassInMemoryIndexWriter();
                                    tokenProcessor = new BetterTokenProcessor();
                                    indexWriter.indexCorpus(corpus, tokenProcessor, Paths.get(corpusPath).toAbsolutePath());


                                } else {
                                    System.out.println("Invalid corpus path. Please enter a valid path : ");
                                    file = null;
                                }
                            }


                        case "2":

                            System.out.println("Select a mode for query ?");
                            System.out.println("1. Boolean Query");
                            System.out.println("2. Ranked Retrivals");
                            System.out.print(">> ");
                            queryMode = reader.readLine();

                            // file = null;
                            //query index
                            if (corpusPath == null) {
                                System.out.print("Enter the corpus path : ");
                                corpusPath = reader.readLine();
                                file = new File(corpusPath);
                            }
                            file = new File(corpusPath);
                            boolean success = false;
                            while (!success) {

                                try {
                                    corpus = DirectoryCorpus.loadJsonDirectory(Paths.get(corpusPath).toAbsolutePath(), ".json");
                                    corpus.getDocuments();
                                    index = new DiskPositionalIndex(Paths.get(file.toPath().toAbsolutePath() + "\\index"));
                                    List<String> unstemmedVocabs = getUnstemmedVocabs(corpusPath);
                                    tokenProcessor = new BetterTokenProcessor();
                                    wildcardIndexer = new KGramIndex(index, unstemmedVocabs, tokenProcessor);
                                    System.out.println("Index read completed.");
                                    success = true;
                                } catch (Exception e) {
                                    System.out.println("Error reading the index from " + file.getAbsolutePath());
                                    System.out.println("Please enter a valid path to continue reading index :");
                                    corpusPath = reader.readLine();

                                }

                            }
                            if (!success) {
                                choice = "3";
                                break;
                            }


                            if (queryMode.equals("2")) {
                                rankedRetrieval = new RankedRetrieval(Paths.get(corpusPath + "//index").toAbsolutePath());
                            }
                            break;

                        case "3":
                            break;


                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;
            case "3":
                System.exit(0);
                break;
        }


        while (!query.equalsIgnoreCase("q")) {
            try {
                tokenProcessor = new BetterTokenProcessor();
                System.out.print("Enter your query: ");
                query = reader.readLine();
                String[] queryLiterals = query.split(" ");
                switch (queryLiterals[0].toLowerCase()) {
                    case "q":
                        break;
                    case "docid":
                        System.out.println("Json Document " + corpus.getDocument(Integer.parseInt(queryLiterals[1])).getName());

                        break;
                    case "stem":
                        snowballStemmer.setCurrent(queryLiterals[1]);
                        snowballStemmer.stem();
                        String stemmedToken = snowballStemmer.getCurrent();
                        System.out.println("The stemmed term: " + stemmedToken);
                        break;
                    case "vocab":
                        List<String> sortedVocabulary = index.getVocabulary().stream().sorted().collect(Collectors.toList());
                        int i = 0;
                        for (String vocab : sortedVocabulary) {
                            System.out.println(vocab);
                            i++;
                            if (i == 999) break;
                        }
                        System.out.println("The count of the total number of vocabulary terms: " + sortedVocabulary.size());
                        break;
                    default:
                        List<Posting> mPostings = null;
                        switch (queryMode) {
                            case "1":
                                component = parser.parseQuery(query, tokenProcessor, wildcardIndexer);
                                mPostings = component.getPostings(index);
                                if (mPostings != null)
                                    for (Posting p : mPostings) {
                                        System.out.println("Json Document " + corpus.getDocument(p.getDocumentId()).getName());
                                    }
                                System.out.println("Total number of documents returned from the query: " +
                                        " " + mPostings.size());

                                break;
                            case "2":
                                rankedRetrieval.doRankedRetrieval(query, corpus, index, tokenProcessor);
                                break;
                        }


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

                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }


    }

    private static Index indexCorpus(DocumentCorpus corpus, TokenProcessor processor) {


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


    private static List<String> getUnstemmedVocabs(String path) throws IOException {

        List<String> vocabs = new ArrayList<>();
        DataInputStream streamVocab = new DataInputStream(new FileInputStream(path + "\\index\\unstemmedVocabs.bin"));
        while (streamVocab.available() > 0) {
            vocabs.add(streamVocab.readUTF());
        }
        return vocabs;
    }


}
