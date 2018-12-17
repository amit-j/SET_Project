package cecs429.index;

import java.util.List;

/**
 * A Posting encapulates a document ID associated with a search query component.
 */
public class Posting {
    private int mDocumentId;
    private List<Integer> mPositions;
    private long termFrequency;

    public Posting(int documentId) {
        mDocumentId = documentId;
    }

    public Posting(int documentId, long termFrequency) {
        this.mDocumentId = documentId;
        this.termFrequency = termFrequency;
    }

    public Posting(int documentId, List<Integer> list) {
        mDocumentId = documentId;
        mPositions = list;
    }

    public long getTermFrequency() {
        return termFrequency;
    }

    public void addPostingPosition(int position) {
        mPositions.add(position);

    }


    public List<Integer> getPositions() {
        return mPositions;
    }

    ;

    public int getDocumentId() {
        return mDocumentId;
    }
}
