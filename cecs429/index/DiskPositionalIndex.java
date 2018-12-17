package cecs429.index;

import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DiskPositionalIndex implements Index {


    RandomAccessFile randomAccessPostings;
    DB db;
    private BTreeMap<String, Long> map;

    public DiskPositionalIndex(Path path) {
        try {
            randomAccessPostings = new RandomAccessFile(path.toAbsolutePath().toString() + "\\postings.bin", "r");


            db = DBMaker.fileDB(path.toAbsolutePath() + "\\database.db").make();
            map = db.treeMap("map.db").
                    keySerializer(Serializer.STRING).
                    valueSerializer(Serializer.LONG).
                    open();



        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private long binarySearchVocab(String term) {

        try {
            if (map.containsKey(term))
                return map.get(term);

        } catch (Exception e) {
            e.printStackTrace();
        }

        /**We wont be using binary search anymore as we now have B*Tree which is better performance.
         *
         * try {
         long vocabTableLength = randomAccessVocabTable.length()/16;
         long i=0, j= vocabTableLength-1;

         while(i <= j){
         long mid = (i+j)/2;
         randomAccessVocabTable.seek(mid*16);
         long start = randomAccessVocabTable.readLong();
         long end = 0;

         if(i != j){
         randomAccessVocabTable.seek(mid*16+16);
         end = randomAccessVocabTable.readLong();

         } else {
         end = randomAccessVocab.getChannel().size();
         }
         long length = end - start;
         randomAccessVocab.seek(start);

         String word = "";
         long temp = 0;
         while(temp < length){
         word = word + (char) randomAccessVocab.readByte();
         temp++;
         }

         int comparison = term.compareTo(word);

         if(comparison == 0){
         randomAccessVocabTable.seek(mid*16+8);
         return randomAccessVocabTable.readLong();

         } else if(comparison < 0){
         if(i == j) break;
         // term is less than mid
         j = mid;
         } else {
         // term is greater than mid
         i = mid+1;
         }
         }
         } catch (IOException e) {
         e.printStackTrace();
         }
         return -1;
         */
        return -1;
    }

    @Override
    public List<Posting> getPostingsWithPositions(String term) {

        List<Posting> postings = new ArrayList<>();
        long postingPosition = binarySearchVocab(term);
        if (postingPosition >= 0) {
            try {
                randomAccessPostings.seek(postingPosition);

                //get document frequency
                int df = randomAccessPostings.readInt();
                long positionInFile = postingPosition + 4;
                int currentDocId;
                int previosDocId = 0;

                for (int i = 0; i < df; i++) {
                    randomAccessPostings.seek(positionInFile);
                    positionInFile = positionInFile + 4;
                    //get document id
                    currentDocId = randomAccessPostings.readInt() + previosDocId;
                    previosDocId = currentDocId;
                    randomAccessPostings.seek(positionInFile);
                    //get term frequency
                    int tf = randomAccessPostings.readInt();
                    positionInFile = positionInFile + 4;
                    List<Integer> positions = new ArrayList<>();

                    int currentPostingPosition;
                    int previousPostingPosition = 0;

                    //get positions of a term using vocabTable.bin and postings.bin
                    for (int j = 0; j < tf; j++) {
                        randomAccessPostings.seek(positionInFile + 4 * j);
                        currentPostingPosition = randomAccessPostings.readInt() + previousPostingPosition;
                        previousPostingPosition = currentPostingPosition;
                        positions.add(currentPostingPosition);
                    }
                    positionInFile = positionInFile + 4 * tf;
                    Posting posting = new Posting(currentDocId, positions);
                    postings.add(posting);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else
            System.out.println("word not found:" + term);

        return postings;
    }

    /*Get postings without positions ie skip positions while fetching postings from postings.bin*/
    @Override
    public List<Posting> getPostings(String term) {
        List<Posting> postings = new ArrayList<>();
        long postingPosition = binarySearchVocab(term);
        if (postingPosition >= 0) {
            try {
                randomAccessPostings.seek(postingPosition);

                //get document frequency
                int df = randomAccessPostings.readInt();
                long positionInFile = postingPosition + 4;
                int currentDocId;
                int previosDocId = 0;

                for (int i = 0; i < df; i++) {
                    randomAccessPostings.seek(positionInFile);
                    positionInFile = positionInFile + 4;
                    //get document id
                    currentDocId = randomAccessPostings.readInt() + previosDocId;
                    previosDocId = currentDocId;
                    randomAccessPostings.seek(positionInFile);
                    //get term frequency
                    int tf = randomAccessPostings.readInt();
                    positionInFile = positionInFile + 4;

                    positionInFile = positionInFile + 4 * tf;
                    Posting posting = new Posting(currentDocId, tf);
                    postings.add(posting);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("word not found:" + term);
        }
        return postings;
    }

    @Override
    public List<String> getVocabulary() {
        return new ArrayList<String>(map.getKeys());
    }

    /*public static void main(String[] arg) throws IOException {
        DiskPositionalIndex test =  new DiskPositionalIndex(Paths.get("C:\\mlb\\1\\index").toAbsolutePath());

        System.out.println("\n*****************************************");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        String q="";
        while(!q.equals("q")){

            List<Posting> postings = test.getPostings(reader.readLine());
        //for(Posting posting: postings){
     //       System.out.print(posting.getDocumentId() + " " );
       // }
        System.out.println("total :"+postings.size());

        }

    }*/

}
