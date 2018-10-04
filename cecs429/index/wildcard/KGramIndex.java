package cecs429.index.wildcard;

import cecs429.index.Index;
import cecs429.index.Posting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class KGramIndex {

    private HashMap<String, List<Integer>> kGramIndex; // integer will point to the location in the original vocab;
    private KGramMaker kGramMaker;
    private Index mIndex;

    public KGramIndex(Index index) {
        kGramIndex = new HashMap<>();
        kGramMaker = new KGramMaker();
        mIndex = index;
        buildKGramIndex();
    }

    public static void sortPostings(List<WildcardPosting> list, int from, int to) {  //expensive but needed to use our simple and merge algo
        //reference : https://stackoverflow.com/questions/30971520/quick-sort-list-in-java
        if (from < to) {
            int pivot = from;
            int left = from + 1;
            int right = to;
            int pivotValue = list.get(pivot).getDocumentId();
            while (left <= right) {
                // left <= to -> limit protection
                while (left <= to && pivotValue >= list.get(left).getDocumentId()) {
                    left++;
                }
                // right > from -> limit protection
                while (right > from && pivotValue < list.get(right).getDocumentId()) {
                    right--;
                }
                if (left < right) {
                    Collections.swap(list, left, right);
                }
            }
            Collections.swap(list, pivot, left - 1);
            sortPostings(list, from, right - 1); // <-- pivot was wrong!
            sortPostings(list, right + 1, to);   // <-- pivot was wrong!
        }
    }

    public List<WildcardPosting> getPostings(String term) {
        List<WildcardPosting> mPosting = new ArrayList<>();
        if (kGramIndex.containsKey(term)) {
            for (Integer i : kGramIndex.get(term)) {
                //for(Posting p:mIndex.getPostings(mIndex.getVocabulary().get(i)))

                if (mPosting.size() == 0) {
                    mPosting.addAll(wildcardPostingAdapter(mIndex.getPostings(mIndex.getVocabulary().get(i)), i));
                    //sortPostings(mPosting, 0, mPosting.size() - 1);
                } else {
                    List<Posting> tempPosting = (mIndex.getPostings(mIndex.getVocabulary().get(i)));
                    mPosting = combinePosting(mPosting, tempPosting, i);
                }


            }

        }
        return mPosting;
    }

    private List<WildcardPosting> combinePosting(List<WildcardPosting> p1, List<Posting> p2, int vocabIndex) {
        List<WildcardPosting> mPosting = new ArrayList<>();
        int i = 0;
        int j = 0;
        while (i < p1.size() && j < p2.size()) {

            if (p1.get(i).getDocumentId() < p2.get(j).getDocumentId())
                mPosting.add(p1.get(i++));
            else if (p1.get(i).getDocumentId() > p2.get(j).getDocumentId())
                mPosting.add(new WildcardPosting(p2.get(j++), vocabIndex));
            else {

                if (p1.get(i).getVocabID() < vocabIndex) {
                    mPosting.add(p1.get(i++));

                } else {
                    mPosting.add(new WildcardPosting(p2.get(j++), vocabIndex));

                }

            }
        }
        while (i < p1.size())
            mPosting.add(p1.get(i++));

        while (j < p2.size())
            mPosting.add(new WildcardPosting(p2.get(j++), vocabIndex));

        return mPosting;

    }

    private List<WildcardPosting> wildcardPostingAdapter(List<Posting> list, int vocabIndex) {
        List<WildcardPosting> wPosting = new ArrayList<>();
        for (Posting p : list)
            wPosting.add(new WildcardPosting(p, vocabIndex));
        return wPosting;
    }

    public List<String> getVocabulary() {
        return null;
    }

    private void buildKGramIndex() {
        int vocabIndex = 0;
        for (String term : mIndex.getVocabulary()) {

            addTerm(term, vocabIndex++);
        }

    }

    private void addTerm(String term, int vocabIndex) {

        for (String splits : term.split("\\*"))
            for (String kgram : kGramMaker.makeKgrams(splits)) {

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
        return mIndex.getVocabulary().get(vocabIndex);
    }
}
