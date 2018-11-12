package cecs429.index.wildcard;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.TokenProcessor;
import jdk.nashorn.internal.parser.Token;

import java.util.*;

public class KGramIndex {

    private HashMap<String, List<Integer>> kGramIndex; // integer will point to the location in the original vocab;
    private KGramMaker kGramMaker;
    private Index mIndex;
    private List<String> vocab; //this will be storing the unprocessed terms, as kgrams for processed words might provide incorrect results.
    private TokenProcessor tokenProcessor;

    public KGramIndex(Index index,List<String> vocabs,TokenProcessor tp) {
        kGramIndex = new HashMap<>();
        kGramMaker = new KGramMaker();
        mIndex = index;
        vocab =vocabs;
        buildKGramIndex();
        tokenProcessor = tp;

    }




    public List<Integer> getVocabIndexforTerm(String term){

        if(kGramIndex.containsKey(term)){
            return kGramIndex.get(term);
        }
        return new ArrayList<>();

    }


    private void buildKGramIndex() {

        int vocabIndex = 0;
        for (String term : vocab) {

            addTerm(term, vocabIndex++);
        }

    }

    private void addTerm(String term, int vocabIndex) {

            for (String kgram : kGramMaker.makeKgrams(term)) {

                if (!kgram.equals(" ") && !kgram.equals("$")) {

                    if (kGramIndex.containsKey(kgram)) {

                        List<Integer> mList = kGramIndex.get(kgram);
                        if (mList.get(mList.size() - 1) != vocabIndex)
                            mList.add(vocabIndex);
                    } else {
                        List<Integer> mList = new ArrayList<>();
                        mList.add(vocabIndex);
                        kGramIndex.put(kgram, mList);
                    }
                }
            }
    }

    public String getWordAt(int vocabIndex) {
        return vocab.get(vocabIndex);
    }

    public TokenProcessor getTokenProcessor(){return tokenProcessor;}
}
