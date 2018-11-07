package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.index.wildcard.KGramIndex;
import cecs429.index.wildcard.WildcardPosting;
import cecs429.text.TokenProcessor;
import jdk.nashorn.internal.parser.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class WildcardLiteral implements QueryComponent {

    private String mTerms;
    private KGramIndex wildcardIndex;

    public WildcardLiteral(String term, KGramIndex wIndex) {
        mTerms = term;
        wildcardIndex = wIndex;

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
    public List<Posting> getPostings(Index index) {

        List<String> breakdowns = breakMaxKGram(mTerms);
        Set<Integer> possibleVocabs = new TreeSet<>();
        List<Integer> matchedVocabs = new ArrayList<>();

        if (breakdowns.size() == 1) { //only one term


            for (int vocab :  wildcardIndex.getVocabIndexforTerm(breakdowns.get(0))) {
                if (matchWildCard(wildcardIndex.getWordAt(vocab), mTerms)) {
                    matchedVocabs.add(vocab);
                }
            }

            List<QueryComponent> terms = new ArrayList<>();

            for (int vocab : matchedVocabs) {
                //now we just create an or component for the possible vocabs and display the postings returned by that
                terms.add(new TermLiteral(wildcardIndex.getWordAt(vocab)));
            }

            OrQuery query = new OrQuery(terms);

            return query.getPostings(index);


        }

        else {

            for (String term : breakdowns) {
                possibleVocabs.addAll(wildcardIndex.getVocabIndexforTerm(term));
            }

            System.out.println("matched with words:");
            for (int vocab : possibleVocabs) {
                if (matchWildCard(wildcardIndex.getWordAt(vocab), mTerms)) {
                    matchedVocabs.add(vocab);
                    System.out.print(wildcardIndex.getWordAt(vocab)+" , ");

                }
            }



            List<QueryComponent> terms = new ArrayList<>();
            //now we just create an or component for the possible vocabs and display the postings returned by that

            for (int vocab : matchedVocabs) {
                    terms.add(new TermLiteral(wildcardIndex.getWordAt(vocab)));
            }

            OrQuery query = new OrQuery(terms);

            return query.getPostings(index);

        }

    }

    @Override
    public Boolean isNegative() {
        return false;
    }

}
