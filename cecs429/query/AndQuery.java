package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An AndQuery composes other QueryComponents and merges their postings in an intersection-like operation.
 */
public class AndQuery implements QueryComponent {
	private List<QueryComponent> mComponents;
	
	public AndQuery(List<QueryComponent> components) {
		mComponents = components;
	}
	
	@Override
	public List<Posting> getPostings(Index index) {

		List<Posting> result = null;
		// TODO: program the merge for an AndQuery, by gathering the postings of the composed QueryComponents and
		// intersecting the resulting postings.
        List<Posting> resultsI = new ArrayList<>();
        List<Posting> resultsJ = new ArrayList<>();

        if(mComponents.size()<1){
            return result;
        }

        else if(mComponents.size()==1){
            return mComponents.get(0).getPostings(index);

        }

        //variables for traversing our phrase terms
        String term1,term2;
        int iResult=0,jResult=0;
        Posting iPosting,jPosting;

        resultsI =  mComponents.get(0).getPostings(index);
        for(int i = 1; i<mComponents.size()-1;i++){

            resultsJ = mComponents.get(i).getPostings(index);

        while((iResult < resultsI.size()-1)  && (jResult < resultsJ.size() -1)){

                    iPosting = resultsI.get(iResult);
                    jPosting = resultsJ.get(jResult);


                    if(iPosting.getDocumentId()<jPosting.getDocumentId()){
                        iResult++;
                    }
                    else if(iPosting.getDocumentId()>jPosting.getDocumentId())
                        jResult++;
                    else{

                        result.add(iPosting);

                        iResult++;
                        jResult++;


                    }

                }


        resultsI = result;


        }



        return result;
	}
	
	@Override
	public String toString() {
		return
		 String.join(" ", mComponents.stream().map(c -> c.toString()).collect(Collectors.toList()));
	}
}
