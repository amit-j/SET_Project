package cecs429.index;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class DiskIndexWriter {

    private List<String> vocabualary;

    public void writeIndex(Index index, Path path){

        try {
            createDirectoryStructre(path);
            writeVocabTable(writePostings(index,path),writeVocab(path),path);
        }
        catch (Exception e){
            System.out.println("Something went wrong.."+e.getMessage());
        }


    }

    private void createDirectoryStructre(Path path) throws IOException{
        File directory = new File(path.toAbsolutePath().toString());
        if (! directory.exists()) {
            Files.createDirectories(path);

        }

    }
    private HashMap<String,Long> writePostings(Index index,Path path) throws IOException,FileNotFoundException {
        int maxDocumentId = 0; //we will keep a track of maximum document id encountered to write the Ld for each document sequentially at the end
        HashMap<String,Long> postingFileLocations = new HashMap<>();
        FileOutputStream fos = new FileOutputStream(path.toAbsolutePath().toString()+"\\postings.bin");
        DataOutputStream stream = new DataOutputStream(fos);

        HashMap<Integer,List<Integer>> documentWeights = new HashMap<>(); //maps document id to list of tfd's.

        vocabualary = index.getVocabulary().stream().sorted().collect(Collectors.toList());
        for(String term: vocabualary){

            postingFileLocations.put(term,fos.getChannel().position());
            List<Posting> postings = index.getPostings(term);
            //gaps start
            stream.writeInt(postings.size());


            int documentIDGap = 0;
            for(Posting posting:postings){

                //check if this is the highest doc id encountered
                if(posting.getDocumentId() > maxDocumentId)
                    maxDocumentId=posting.getDocumentId();


                //write document id with gaps
                stream.writeInt(posting.getDocumentId()-documentIDGap);

                //check if the term exists in the document id already exists in the document weight hashmap and handle adding to its list
                if(documentWeights.containsKey(posting.getDocumentId())){
                    List<Integer> weightList =  documentWeights.get(posting.getDocumentId());
                    weightList.add(posting.getPositions().size());


                }
                else{
                    List<Integer> weightList = new ArrayList<>();
                    weightList.add(posting.getPositions().size());
                    documentWeights.put(posting.getDocumentId(),weightList);

                }

                documentIDGap = posting.getDocumentId();

                //write document term freq
                stream.writeInt(posting.getPositions().size());


                //write term positions
                int positionGap = 0;
                for(Integer position:posting.getPositions()){

                    stream.writeInt(position-positionGap);
                    positionGap = position;
                }
            }


        }
        stream.close();


        //all the tfds have been found out, calculate and store Ld

         fos = new FileOutputStream(path.toAbsolutePath().toString()+"\\docWeights.bin");
         stream = new DataOutputStream(fos);

        for(int i=0;i<=maxDocumentId;i++){

            if(documentWeights.containsKey(i)) {
                List<Integer> documentTermFreqeuncyList = documentWeights.get(i);
                Double ld = new Double(0);

                for (Integer freq : documentTermFreqeuncyList) {

                    Double log = 1 + Math.log(freq);
                    ld += log * log;


                }
                Double ldsqrt = Math.sqrt(ld);
                stream.writeDouble(ldsqrt);
            }
            else{

                stream.writeDouble(0);
            }



        }
        stream.close();


    return postingFileLocations;

    }
    private HashMap<String,Long> writeVocab(Path path) throws FileNotFoundException,IOException{
        FileOutputStream fos = new FileOutputStream(path.toAbsolutePath().toString()+"\\vocabs.bin");
        DataOutputStream stream = new DataOutputStream(fos);
       HashMap<String,Long> vocabMap = new HashMap();
        for(String term:vocabualary){
            vocabMap.put(term,fos.getChannel().position());
            stream.writeBytes(term);
        }
        stream.close();
        return vocabMap;

    }
    private void writeVocabTable(HashMap<String,Long> postings,HashMap<String,Long> vocab,Path path) throws FileNotFoundException,IOException {

        DataOutputStream stream = new DataOutputStream(new FileOutputStream(path.toAbsolutePath().toString()+"\\vocabTable.bin"));

        for(String term:vocabualary){
            stream.writeLong(vocab.get(term));
            stream.writeLong(postings.get(term));

        }
        stream.close();

    }


}
