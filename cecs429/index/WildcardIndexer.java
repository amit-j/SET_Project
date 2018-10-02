package cecs429.index;

import javafx.geometry.Pos;

public class WildcardIndexer {

    private PositionalInvertedIndex mIndex;
    private KGramMaker kGramMaker;

    public  WildcardIndexer(PositionalInvertedIndex index){
        mIndex = index;
        kGramMaker = new KGramMaker();
    }



    public void addTerm(String term,int docID,int position){
        for(String splits:term.split("\\*"))
            for(String kgram:kGramMaker.makeKgrams(splits)){

                if(!kgram.equals(" ") && !kgram.equals("$")){
                    mIndex.addTerm(kgram,docID,position);
                }

            }
    }



    public Index getIndex(){return mIndex;}




}
