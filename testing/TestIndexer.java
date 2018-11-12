package testing;

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

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class TestIndexer {

    QueryComponent component ;
    BooleanQueryParser parser ;
    DocumentCorpus corpus;
    Index index;
    KGramIndex wildcardIndexer;

    void configure(){
        parser = new BooleanQueryParser();
        corpus = DirectoryCorpus.loadTextDirectory(Paths.get("testcases").toAbsolutePath(), ".txt");
        index = indexCorpus(corpus);
       // wildcardIndexer = new KGramIndex(index);
    }

    int executeQuery(String query){

        component = parser.parseQuery(query,new BetterTokenProcessor(), wildcardIndexer);
       // System.out.println(component.getPostings(index).size());
        for (Posting p : component.getPostings(index)) {
            // System.out.println("Json Document " + corpus.getDocument(p.getDocumentId()).getTitle());
        }
        return component.getPostings(index).size();
    }

    private static Index indexCorpus(DocumentCorpus corpus) {
        BetterTokenProcessor processor = new BetterTokenProcessor();

        PositionalInvertedIndex index = new PositionalInvertedIndex();

        System.out.println("started reading document:");
        long start = System.currentTimeMillis();
        int documentCount = 0;
        for(Document document:corpus.getDocuments()){
//            if(documentCount>10){
//                break;
//            }
            documentCount++;

            EnglishTokenStream tokenStream = new EnglishTokenStream(document.getContent());
            //System.out.println("reading document: "+document.getTitle());

            int position = 0;
            for(String token:tokenStream.getTokens()){
                for(String term : processor.processToken(token)) {
                    index.addTerm(term, document.getId(), position++);


                }
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("Indexing process took: " + ((end - start) / 1000)+" seconds");


        return index;
    }

    Index handBuiltIndex(){

        //Index index = new PositionalInvertedIndex();
        HashMap<String, List<Posting>> mIndex = new HashMap<>();

        List<Posting> postingList = new ArrayList<>();
        List<Integer> list1 = new ArrayList<>();
        list1.add(19);
        Posting posting1 = new Posting(0,list1);
        List<Integer> list2 = new ArrayList<>();
        list2.add(23);
        Posting posting2 = new Posting(4,list2);
        postingList.add(posting1);
        postingList.add(posting2);

        mIndex.put("tiger&tigress",postingList);

        List<Posting> postingList2 = new ArrayList<>();
        List<Integer> list3 = new ArrayList<>(Arrays.asList(4,11));

        Posting posting3 = new Posting(1,list3);
        List<Integer> list4 = new ArrayList<>();
        list4.add(5);
        Posting posting4 = new Posting(1,list4);
        postingList2.add(posting3);
        postingList2.add(posting4);

        mIndex.put("and",postingList2);

        List<Posting> postingList3 = new ArrayList<>();
        List<Integer> list5 = new ArrayList<>(Arrays.asList(3, 10));
        Posting posting8 = new Posting(2,list5);
        List<Integer> list6 = new ArrayList<>(Arrays.asList(3, 14));
        Posting posting9 = new Posting(3,list6);
        postingList3.add(posting8);
        postingList3.add(posting9);

        mIndex.put("lion",postingList3);


        List<Posting> postingList4 = new ArrayList<>();
        List<Integer> list7 = new ArrayList<>(Arrays.asList(2, 5, 12, 17));
        Posting posting5 = new Posting(0,list7);
        List<Integer> list8 = new ArrayList<>(Arrays.asList(7, 9, 14, 15));
        Posting posting6 = new Posting(2,list8);
        List<Integer> list9 = new ArrayList<>(Arrays.asList(8, 12, 15));
        Posting posting7 = new Posting(4,list9);
        postingList4.add(posting5);
        postingList4.add(posting6);
        postingList4.add(posting7);

        mIndex.put("monkey",postingList4);

        List<Posting> postingList5 = new ArrayList<>();
        List<Integer> list10 = new ArrayList<>(Arrays.asList(6, 13));
        Posting posting10 = new Posting(4,list10);
        postingList5.add(posting10);
        mIndex.put("tigress",postingList5);

        List<Posting> postingList6 = new ArrayList<>();
        List<Integer> list11 = new ArrayList<>(Arrays.asList(4));
        Posting posting11 = new Posting(4,list11);
        postingList6.add(posting11);
        mIndex.put("tiger",postingList6);

        List<Posting> postingList7 = new ArrayList<>();
        List<Integer> list12 = new ArrayList<>(Arrays.asList(5, 12));
        Posting posting12 = new Posting(2,list12);
        postingList7.add(posting12);
        mIndex.put("lioness",postingList7);


        List<Posting> postingList8 = new ArrayList<>();
        List<Integer> list13 = new ArrayList<>(Arrays.asList(4, 11));
        Posting posting13 = new Posting(2,list13);
        List<Integer> list14 = new ArrayList<>(Arrays.asList(5));
        Posting posting14 = new Posting(4,list14);
        postingList8.add(posting13);
        postingList8.add(posting14);
        mIndex.put("and",postingList8);


        List<Posting> postingList9 = new ArrayList<>();
        List<Integer> list24 = new ArrayList<>(Arrays.asList(3, 7, 14, 18));
        Posting posting15 = new Posting(0,list24);
        List<Integer> list15 = new ArrayList<>(Arrays.asList(1, 7));
        Posting posting16 = new Posting(1,list15);
        List<Integer> list16 = new ArrayList<>(Arrays.asList(10));
        Posting posting17 = new Posting(4,list16);
        postingList9.add(posting15);
        postingList9.add(posting16);
        postingList9.add(posting17);
        mIndex.put("cat",postingList9);

        List<Posting> postingList10 = new ArrayList<>();
        List<Integer> list17 = new ArrayList<>(Arrays.asList(4, 10, 11));
        Posting posting18 = new Posting(0,list17);
        List<Integer> list18 = new ArrayList<>(Arrays.asList(2, 6, 10, 13));
        Posting posting19 = new Posting(3,list18);
        List<Integer> list19 = new ArrayList<>(Arrays.asList(22));
        Posting posting20 = new Posting(4,list19);
        postingList10.add(posting18);
        postingList10.add(posting19);
        postingList10.add(posting20);
        mIndex.put("fish",postingList10);


         postingList = new ArrayList<>();
        List<Integer> listPet = new ArrayList<>(Arrays.asList(0,15));
        Posting postingPet = new Posting(0,listPet);
        postingList.add(postingPet);
        mIndex.put("pet",postingList);

         postingList = new ArrayList<>();
        List<Integer> listWild = new ArrayList<>(Arrays.asList(0));
        Posting postingWild = new Posting(2,listWild);
        mIndex.put("wild",postingList);

        //dog
        postingList = new ArrayList<>();
        List<Integer> listdog = new ArrayList<>(Arrays.asList(1,6,9,13,16));
        Posting postingdog= new Posting(0,listdog);
        postingList.add(postingdog);

        listdog = new ArrayList<>(Arrays.asList(5,7,9,11));
        postingdog = new Posting(3,listdog);
        postingList.add(postingdog);

        listdog = new ArrayList<>(Arrays.asList(3,21));
        postingdog = new Posting(4,listdog);
        postingList.add(postingdog);
        mIndex.put("dog",postingList);


        //Anim
        postingList = new ArrayList<>();
        List<Integer> listAnim = new ArrayList<>(Arrays.asList(0));
        Posting postingAnim = new Posting(1,listAnim);
        postingList.add(postingAnim);


        listAnim = new ArrayList<>(Arrays.asList(1,16));
        postingAnim = new Posting(2,listAnim);
        postingList.add(postingAnim);

        listAnim = new ArrayList<>(Arrays.asList(0,15));
        postingAnim = new Posting(3,listAnim);
        postingList.add(postingAnim);

        listAnim = new ArrayList<>(Arrays.asList(0,17));
        postingAnim = new Posting(4,listAnim);
        postingList.add(postingAnim);
        mIndex.put("anim",postingList);


        //horse
        postingList = new ArrayList<>();
        List<Integer> listHorse = new ArrayList<>(Arrays.asList(3,5));
        Posting postingHorse = new Posting(1,listHorse);
        postingList.add(postingHorse);


        listHorse = new ArrayList<>(Arrays.asList(2,6,13,18));
        postingHorse = new Posting(2,listHorse);
        postingList.add(postingHorse);

        listHorse = new ArrayList<>(Arrays.asList(1,12));
        postingHorse = new Posting(3,listHorse);
        postingList.add(postingHorse);

        listHorse = new ArrayList<>(Arrays.asList(1,7,11,14,18));
        postingHorse = new Posting(4,listHorse);
        postingList.add(postingHorse);
        mIndex.put("horse",postingList);



        //Eleph
        postingList = new ArrayList<>();
        List<Integer> listEleph = new ArrayList<>(Arrays.asList(8));
        Posting postingEleph = new Posting(0,listEleph);
        postingList.add(postingEleph);


        listEleph = new ArrayList<>(Arrays.asList(2,4,6,8));
        postingEleph = new Posting(1,listEleph);
        postingList.add(postingEleph);

        listEleph = new ArrayList<>(Arrays.asList(8,17));
        postingEleph = new Posting(2,listEleph);
        postingList.add(postingEleph);

        listEleph = new ArrayList<>(Arrays.asList(4,8));
        postingEleph = new Posting(3,listEleph);
        postingList.add(postingEleph);

        listEleph = new ArrayList<>(Arrays.asList(2,8,16,19,20));
        postingEleph = new Posting(4,listEleph);
        postingList.add(postingEleph);
        mIndex.put("eleph",postingList);


//        eleph 0 [8]
//        eleph 1 [2, 4, 6, 8]
//        eleph 2 [8, 17]
//        eleph 3 [4, 8]
//        eleph 4 [2, 9, 16, 19, 20]
//        hors 1 [3, 5]
//        hors 2 [2, 6, 13, 18]
//        hors 3 [1, 12]
//        hors 4 [1, 7, 11, 14, 18]
//        anim 1 [0]
//        anim 2 [1, 16]
//        anim 3 [0, 15]
//        anim 4 [0, 17]
//        dog 0 [1, 6, 9, 13, 16]
//        dog 3 [5, 7, 9, 11]
//        dog 4 [3, 21]
//        wild 2 [0]
//        pet 0 [0, 15]

        return index;
    }



    public Boolean compareLists(List<Integer> l1,List<Integer> l2){
        for(Integer i:l1)
            if(!l2.contains(i))
                return false;

            return true;
    }

    public List<Posting> getPostings(String term) {
        return index.getPostings(term);
    }


}
