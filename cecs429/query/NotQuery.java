package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;

import java.util.List;

public class NotQuery implements QueryComponent {

    private QueryComponent mComponent;

    public NotQuery(QueryComponent component) {
        mComponent = component;
    }

    @Override
    public List<Posting> getPostings(Index index) {
        return mComponent.getPostings(index);
    }

    @Override
    public Boolean isNegative() {
        return true;
    }


}
