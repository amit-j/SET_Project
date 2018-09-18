package cecs429.index;

import javafx.geometry.Pos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class InvertedIndex implements  Index{

    private HashMap<String,List<Posting>> mIndex;


    public InvertedIndex(){
        mIndex = new HashMap<>();
    }

    @Override
    public List<String> getVocabulary() {

        List<String> mList  = new ArrayList<>();

        for(String s:mIndex.keySet()){
            mList.add(s);

        }
return mList;
    }

    public void addTerm(String term, int documentId) {

        if(mIndex.containsKey(term)){

            List<Posting> mList = mIndex.get(term);
            if(mList.get(mList.size()-1).getDocumentId()!=documentId){
                mList.add(new Posting(documentId));
            }
        }
        else{
            List<Posting> mList = new ArrayList<Posting>();
            mList.add(new Posting(documentId));
            mIndex.put(term,mList);

        }
    }

    @Override
    public List<Posting> getPostings(String term) {

        if(mIndex.containsKey(term)) return mIndex.get(term);
        return new ArrayList<Posting>();


    }

}
