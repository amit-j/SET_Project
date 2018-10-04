package cecs429.index.wildcard;

import cecs429.index.Posting;

import java.util.List;

public class WildcardPosting extends Posting {

    int mVocabIndex;
    public WildcardPosting(int documentId) {
        super(documentId);
    }

    public WildcardPosting(int documentId, List<Integer> list) {
        super(documentId, list);
    }

    public WildcardPosting(Posting posting,int vocabID) {
        super(posting.getDocumentId(), posting.getPositions());
        mVocabIndex = vocabID;
    }

    public int getVocabID(){
        return mVocabIndex;
    }

}
