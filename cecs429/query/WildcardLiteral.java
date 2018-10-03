package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;

import java.util.ArrayList;
import java.util.List;

public class WildcardLiteral implements QueryComponent {

    private String mTerms;
    private  Index wildcardIndex;

    public WildcardLiteral(String term,Index wIndex) {
        mTerms = term;
        wildcardIndex = wIndex;
    }


    @Override
    public List<Posting> getPostings(Index index) {

        List<Posting> mPostings = new ArrayList<Posting>();
        List<String> breakdowns = breakMaxKGram(mTerms);

        if(breakdowns.size()==1){

            return wildcardIndex.getPostings(breakdowns.get(0));
        }

       List<Posting> termOnePostings = wildcardIndex.getPostings(breakdowns.get(0));

        int currentPosting = 1;

        while(currentPosting<breakdowns.size()) {
            List<Posting> termTwoPosting = wildcardIndex.getPostings(breakdowns.get(currentPosting));

            //intersect the postings
            //copying the same merge as AND Query
            int iResult = 0;
            int jResult = 0;
            while ((iResult < termOnePostings.size()) && (jResult < termTwoPosting.size())) {

                Posting iPosting = termOnePostings.get(iResult);
                Posting jPosting = termTwoPosting.get(jResult);


                if (iPosting.getDocumentId() < jPosting.getDocumentId()) {
                    iResult++;
                } else if (iPosting.getDocumentId() > jPosting.getDocumentId())
                    jResult++;
                else {

                    int iPositionIndex=0,jPositionIndex=0;

                    while((iPositionIndex < iPosting.getPositions().size())  && (jPositionIndex < jPosting.getPositions().size())){

                        int iPosition = iPosting.getPositions().get(iPositionIndex);
                        int jPosition = jPosting.getPositions().get(jPositionIndex);

                        if(iPosition == jPosition){

                            if(mPostings.size() == 0 )
                            {
                                mPostings.add(jPosting);
                            }
                            else{

                                if(mPostings.get(mPostings.size()-1).getDocumentId() < iPosting.getDocumentId())
                                    mPostings.add(jPosting);

                            }
                            break;
                        }


                        if(iPosition<jPosition)
                            iPositionIndex++;
                        else
                            jPositionIndex++;


                    }

                    iResult++;
                    jResult++;


                }

            }


            termOnePostings = mPostings;
            currentPosting++;
            mPostings = new ArrayList<>();
        }



        return termOnePostings;
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
            if(!splits.substring(startIndex, splits.length()).equals("$"))
                 kgrams.add(splits.substring(startIndex, splits.length()));


        }
        return kgrams;
    }


    private boolean verifyWildcardMatch(String query, String term){
        return false;
    }

    @Override
    public Boolean isNegative(){
        return false;
    }
}
