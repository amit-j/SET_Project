package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;

import java.util.List;

public class NotQuery implements QueryComponent{

    private String mTerm;

    public NotQuery(String term) {
        mTerm = term;
    }

    @Override
    public List<Posting> getPostings(Index index) {
        return index.getPostings(mTerm);
    }

    @Override
    public Boolean isNegative() {
        return true;
    }


}
