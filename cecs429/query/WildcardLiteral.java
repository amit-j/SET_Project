package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.index.wildcard.KGramIndex;
import cecs429.index.wildcard.WildcardPosting;

import java.util.ArrayList;
import java.util.List;

public class WildcardLiteral implements QueryComponent {

    private String mTerms;
    private KGramIndex wildcardIndex;

    public WildcardLiteral(String term, KGramIndex wIndex) {
        mTerms = term;
        wildcardIndex = wIndex;
    }


    @Override
    public List<Posting> getPostings(Index index) {

        List<WildcardPosting> mPostings = new ArrayList<>();
        List<String> breakdowns = breakMaxKGram(mTerms);

        if (breakdowns.size() == 1) {

            mPostings = wildcardIndex.getPostings(breakdowns.get(0));


            List<Posting> verifiedPosting = verifyWildcardMatch(mPostings, mTerms);
            List<Posting> resultPostings = new ArrayList<>();

            for (Posting p : verifiedPosting) {
                if (resultPostings.size() == 0)
                    resultPostings.add(p);
                else {
                    if (resultPostings.get(resultPostings.size() - 1).getDocumentId() != p.getDocumentId())
                        resultPostings.add(p);
                }
            }

            return resultPostings;
        } else {

            List<WildcardPosting> termOnePostings = wildcardIndex.getPostings(breakdowns.get(0));

            int currentPosting = 1;

            while (currentPosting < breakdowns.size()) {
                List<WildcardPosting> termTwoPosting = wildcardIndex.getPostings(breakdowns.get(currentPosting));

                //intersect the postings
                //copying the same merge as AND Query
                int iResult = 0;
                int jResult = 0;

                while ((iResult < termOnePostings.size()) && (jResult < termTwoPosting.size())) {

                    WildcardPosting iPosting = termOnePostings.get(iResult);
                    WildcardPosting jPosting = termTwoPosting.get(jResult);
                    String wordI = wildcardIndex.getWordAt(iPosting.getVocabID());
                    String wordJ = wildcardIndex.getWordAt(jPosting.getVocabID());

                    if (iPosting.getDocumentId() < jPosting.getDocumentId()) {
                        iResult++;
                    } else if (iPosting.getDocumentId() > jPosting.getDocumentId())
                        jResult++;
                    else {

                        if (iPosting.getVocabID() < jPosting.getVocabID())
                            iResult++;
                        else if (iPosting.getVocabID() > jPosting.getVocabID())
                            jResult++;
                        else {
                            if (mPostings.size() == 0) {
                                mPostings.add(jPosting);
                            } else {

                                // if (mPostings.get(mPostings.size() - 1).getDocumentId() < iPosting.getDocumentId())
                                mPostings.add(jPosting);

                            }
                            iResult++;
                            jResult++;
                        }


                    }

                }


                termOnePostings = mPostings;
                currentPosting++;
                mPostings = new ArrayList<>();
            }


            return verifyWildcardMatch(termOnePostings, mTerms);
        }
    }


    private List<String> breakMaxKGram(String term) {

        List<String> kgrams = new ArrayList<>();
        term = "$" + term + "$";

        for (String splits : term.split("\\*")) {
            int startIndex = 0;
            while (startIndex + 3 < splits.length()) {
                kgrams.add(splits.substring(startIndex, startIndex + 3));
                startIndex++;
            }
            if (!splits.substring(startIndex, splits.length()).equals("$"))
                kgrams.add(splits.substring(startIndex, splits.length()));


        }
        return kgrams;
    }


    private List<Posting> verifyWildcardMatch(List<WildcardPosting> wildcardPostings, String search) {
        List<Posting> mResults = new ArrayList<>();
        for (WildcardPosting wPosting : wildcardPostings) {
            String termVocab = wildcardIndex.getWordAt(wPosting.getVocabID());
            if (matchWildCard(termVocab, search))
                mResults.add(wPosting);

        }
        return mResults;
    }


    private Boolean matchWildCard(String term, String wildcard) {
        //ref : https://www.geeksforgeeks.org/wildcard-pattern-matching/
        char[] str = term.toCharArray();
        char[] pattern = wildcard.toCharArray();

        int writeIndex = 0;
        boolean isFirst = true;
        for (int i = 0; i < pattern.length; i++) {
            if (pattern[i] == '*') {
                if (isFirst) {
                    pattern[writeIndex++] = pattern[i];
                    isFirst = false;
                }
            } else {
                pattern[writeIndex++] = pattern[i];
                isFirst = true;
            }
        }

        boolean T[][] = new boolean[str.length + 1][writeIndex + 1];

        if (writeIndex > 0 && pattern[0] == '*') {
            T[0][1] = true;
        }

        T[0][0] = true;

        for (int i = 1; i < T.length; i++) {
            for (int j = 1; j < T[0].length; j++) {
                if (pattern[j - 1] == '?' || str[i - 1] == pattern[j - 1]) {
                    T[i][j] = T[i - 1][j - 1];
                } else if (pattern[j - 1] == '*') {
                    T[i][j] = T[i - 1][j] || T[i][j - 1];
                }
            }
        }

        return T[str.length][writeIndex];
    }


    @Override
    public Boolean isNegative() {
        return false;
    }

}
