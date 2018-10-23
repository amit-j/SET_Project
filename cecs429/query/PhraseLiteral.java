package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a phrase literal consisting of one or more terms that must occur in sequence.
 */
public class PhraseLiteral implements QueryComponent {
    // The list of individual terms in the phrase.
    private List<String> mTerms = new ArrayList<>();

    /**
     * Constructs a PhraseLiteral with the given individual phrase terms.
     */
    public PhraseLiteral(List<String> terms) {
        mTerms.addAll(terms);
    }

    /**
     * Constructs a PhraseLiteral given a string with one or more individual terms separated by spaces.
     */
    public PhraseLiteral(String terms) {
        mTerms.addAll(Arrays.asList(terms.split(" ")));
    }

    @Override
    public List<Posting> getPostings(Index index) {

        // TODO: program this method. Retrieve the postings for the individual terms in the phrase,
        // and positional merge them together.
        List<Posting> resultsTermOne = new ArrayList<>();
        List<Posting> resultsTermTwo = new ArrayList<>();
        if (mTerms.size() < 1) {
            return resultsTermOne;
        } else if (mTerms.size() == 1) {
            return index.getPostings(mTerms.get(0));

        }

        //variables for traversing our phrase terms
        String term1, term2;
        int iResult = 0, jResult = 0;
        Posting iPosting, jPosting;
        List<Posting> results = new ArrayList<>();
        term1 = mTerms.get(0);
        resultsTermOne = index.getPostings(term1);

        for (int i = 1; i < mTerms.size(); i++) {

            if (mTerms.size() > i) {
                term2 = mTerms.get(i);
                resultsTermTwo = index.getPostings(term2);

                iResult = 0;
                jResult = 0;
                while ((iResult < resultsTermOne.size()) && (jResult < resultsTermTwo.size())) {

                    iPosting = resultsTermOne.get(iResult);
                    jPosting = resultsTermTwo.get(jResult);


                    if (iPosting.getDocumentId() < jPosting.getDocumentId()) {
                        iResult++;
                    } else if (iPosting.getDocumentId() > jPosting.getDocumentId())
                        jResult++;
                    else {

                        int iPositionIndex = 0, jPositionIndex = 0;

                        while ((iPositionIndex < iPosting.getPositions().size()) && (jPositionIndex < jPosting.getPositions().size())) {

                            int iPosition = iPosting.getPositions().get(iPositionIndex);
                            int jPosition = jPosting.getPositions().get(jPositionIndex);

                            if (iPosition == jPosition - 1) {

                                if (results.size() == 0 || results.get(results.size() - 1).getDocumentId() < jPosting.getDocumentId()) {
                                    List<Integer> mPositions = new ArrayList();
                                    mPositions.add(jPosition);
                                    results.add(new Posting(jPosting.getDocumentId(), mPositions));//its important that we add the last posting so we can compare it with the next term going ahead!!
                                } else {

                                    Posting mPosting = results.get(results.size() - 1);
                                    mPosting.getPositions().add(jPosition);

                                }

                                iPositionIndex++;
                                jPositionIndex++;
                            } else if (iPosition < jPosition)
                                iPositionIndex++;
                            else
                                jPositionIndex++;


                        }

                        iResult++;
                        jResult++;


                    }

                }


                resultsTermOne = results;
                results = new ArrayList<>();

            }
        }

        return resultsTermOne;

    }


    @Override
    public String toString() {
        return "\"" + String.join(" ", mTerms) + "\"";
    }

    @Override
    public Boolean isNegative() {
        return false;
    }
}
