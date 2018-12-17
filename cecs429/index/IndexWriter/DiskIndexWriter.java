package cecs429.index.IndexWriter;

import cecs429.index.Index;
import cecs429.index.Posting;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DiskIndexWriter {

    DB db;
    private List<String> vocabualary;
    private BTreeMap<String, Long> map;
    private DataOutputStream stream;

    public void writeIndex(Index index, Path path) {

        try {
            createDirectoryStructre(path);
            writeVocabTable(writePostings(index, path), writeVocab(path), path);
        } catch (Exception e) {
            System.out.println("Something went wrong.." + e.getMessage());
        }


    }

    private void createDirectoryStructre(Path path) throws IOException {
        File directory = new File(path.toAbsolutePath().toString());
        if (!directory.exists()) {
            Files.createDirectories(path);

        }

    }

    private HashMap<String, Long> writePostings(Index index, Path path) throws IOException {
        int maxDocumentId = 0; //we will keep a track of maximum document id encountered to write the Ld for each document sequentially at the end
        HashMap<String, Long> postingFileLocations = new HashMap<>();
        FileOutputStream fos = new FileOutputStream(path.toAbsolutePath().toString() + "\\postings.bin");
        DataOutputStream stream = new DataOutputStream(fos);

        HashMap<Integer, List<Integer>> documentWeights = new HashMap<>(); //maps document id to list of tfd's.

        vocabualary = index.getVocabulary().stream().sorted().collect(Collectors.toList());
        for (String term : vocabualary) {

            postingFileLocations.put(term, fos.getChannel().position());
            List<Posting> postings = index.getPostings(term);
            //gaps start
            stream.writeInt(postings.size());


            int documentIDGap = 0;
            for (Posting posting : postings) {

                //check if this is the highest doc id encountered
                if (posting.getDocumentId() > maxDocumentId)
                    maxDocumentId = posting.getDocumentId();


                //write document id with gaps
                // stream.writeInt(posting.getDocumentId()-documentIDGap);
                //we dont write gaps since we will be creating buckets
                //merging the buckets will take care of gaps
                stream.writeInt(posting.getDocumentId());

                //check if the term exists in the document id already exists in the document weight hashmap and handle adding to its list
                if (documentWeights.containsKey(posting.getDocumentId())) {
                    List<Integer> weightList = documentWeights.get(posting.getDocumentId());
                    weightList.add(posting.getPositions().size());


                } else {
                    List<Integer> weightList = new ArrayList<>();
                    weightList.add(posting.getPositions().size());
                    documentWeights.put(posting.getDocumentId(), weightList);

                }

                documentIDGap = posting.getDocumentId();

                //write document term freq
                stream.writeInt(posting.getPositions().size());


                //write term positions
                int positionGap = 0;
                for (Integer position : posting.getPositions()) {

                    //stream.writeInt(position-positionGap);
                    //we dont write gaps since we will be creating buckets
                    //merging the buckets will take care of gaps
                    //we will create seperate methods for bucket creation if we have time left.
                    stream.writeInt(position);
                    positionGap = position;
                }
            }


        }
        stream.close();


        return postingFileLocations;

    }

    private HashMap<String, Long> writeVocab(Path path) throws IOException {
        FileOutputStream fos = new FileOutputStream(path.toAbsolutePath().toString() + "\\vocabs.bin");
        DataOutputStream stream = new DataOutputStream(fos);
        HashMap<String, Long> vocabMap = new HashMap();
        for (String term : vocabualary) {
            vocabMap.put(term, fos.getChannel().position());
            stream.writeUTF(term);
        }
        stream.close();
        return vocabMap;

    }

    private void writeVocabTable(HashMap<String, Long> postings, HashMap<String, Long> vocab, Path path) throws FileNotFoundException, IOException {

        DataOutputStream stream = new DataOutputStream(new FileOutputStream(path.toAbsolutePath().toString() + "\\vocabTable.bin"));

        for (String term : vocabualary) {
            stream.writeLong(vocab.get(term));
            stream.writeLong(postings.get(term));

        }
        stream.close();

    }


    public void initDBStore(Path path) {

        File file = new File(path.toAbsolutePath() + "\\index\\database.db");
        if (file.exists()) {
            file.delete();
        }


        db = DBMaker.fileDB(path.toAbsolutePath() + "\\index\\database.db").make();
        map = db.treeMap("map.db").
                keySerializer(Serializer.STRING).
                valueSerializer(Serializer.LONG).
                create();


    }


    //we write unstemmed vocabs to disk so we can create K grams next we read the index from disk
    public void writeUnprocessedVocabs(Path path, Set<String> vocabs) throws IOException {
        FileOutputStream fos = new FileOutputStream(path.toAbsolutePath().toString() + "\\index\\unstemmedVocabs.bin");
        DataOutputStream stream = new DataOutputStream(fos);

        for (String term : vocabs) {
            stream.writeUTF(term);
        }

        stream.close();


    }


    //we use this method for writing the vocab to BTree
    public void writeMergedVocab(String term) {

        map.put(term, new Long(stream.size()));

    }

    public void closeDBStore() {
        db.commit();
        db.close();

    }


    public void initMergedPostingsStream(Path path) throws IOException {

        stream = new DataOutputStream(new FileOutputStream(path.toAbsolutePath() + "\\index\\postings.bin"));


    }

    public void writeMergedPostings(List<Integer> documentIDs, HashMap<Integer, List<Integer>> positions) throws IOException {
        int documentGap = 0;
        stream.writeInt(documentIDs.size());
        for (int docID : documentIDs) {
            stream.writeInt(docID - documentGap);
            stream.writeInt(positions.get(docID).size());
            int positionsGap = 0;
            for (int pos : positions.get(docID)) {
                stream.writeInt(pos - positionsGap);
                positionsGap = pos;
            }
            documentGap = docID;
        }

    }


}
