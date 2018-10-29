package cecs429.index;

import java.io.*;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class DiskIndexWriter {

    private List<String> vocabualary;

    public void writeIndex(Index index, Path path){

        try {

            writeVocabTable(writePostings(index,path),writeVocab(path),path);
        }
        catch (Exception e){
            System.out.println("Something went wrong.."+e.getMessage());
        }


    }


    private HashMap<String,Long> writePostings(Index index,Path path) throws IOException,FileNotFoundException {
        HashMap<String,Long> postingFileLocations = new HashMap<>();
        FileOutputStream fos = new FileOutputStream(path.toAbsolutePath().toString()+"\\postings.bin");
        DataOutputStream stream = new DataOutputStream(fos);

        vocabualary = index.getVocabulary().stream().sorted().collect(Collectors.toList());
        for(String term: vocabualary){

            postingFileLocations.put(term,fos.getChannel().position());
            List<Posting> postings = index.getPostings(term);
            //gaps start
            stream.writeInt(postings.size());


            int documentIDGap = 0;
            for(Posting posting:postings){

                //write document id
                stream.writeInt(posting.getDocumentId()-documentIDGap);

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
