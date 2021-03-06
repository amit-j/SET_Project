package cecs429.index;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PositionalInvertedIndex implements Index {

    private HashMap<String, List<Posting>> mIndex;
    private List<String> mVocab;

    public PositionalInvertedIndex() {
        mIndex = new HashMap<>();
    }

    @Override
    public List<String> getVocabulary() {

        if (mVocab == null) {
            List<String> mList = new ArrayList<>();


            for (String s : mIndex.keySet()) {
                mList.add(s);

            }
            mVocab = mList;
        }

        return mVocab;
    }

    public void addTerm(String term, int documentId, int position) {

        if (mIndex.containsKey(term)) {

            List<Posting> mList = mIndex.get(term);

            if ((mList.get(mList.size() - 1).getDocumentId() != documentId)) {
                // DocumentId is not present in Positional Posting List,
                // this is time when we encounter term in this documentId

                List<Integer> positions = new ArrayList<>();
                positions.add(position);
                mList.add(new Posting(documentId, positions));
            } else {

                mList.get(mList.size() - 1).addPostingPosition(position);

            }
        } else {
            List<Posting> mList = new ArrayList<>();
            List<Integer> positions = new ArrayList<>();
            positions.add(position);
            mList.add(new Posting(documentId, positions));
            mIndex.put(term, mList);

        }
    }

    @Override
    public List<Posting> getPostings(String term) {

        if (mIndex.containsKey(term)) return mIndex.get(term);
        return new ArrayList<>();


    }

    @Override
    public List<Posting> getPostingsWithPositions(String term) {
        return getPostings(term);
    }

}
