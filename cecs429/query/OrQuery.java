package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An OrQuery composes other QueryComponents and merges their postings with a union-type operation.
 */
public class OrQuery implements QueryComponent {
	// The components of the Or query.
	private List<QueryComponent> mComponents;
	
	public OrQuery(List<QueryComponent> components) {
		mComponents = components;
	}
	
	@Override
	public List<Posting> getPostings(Index index) {
		List<Posting> result = null;
		
		// TODO: program the merge for an OrQuery, by gathering the postings of the composed QueryComponents and
		// unioning the resulting postings.

		List<Posting> resultsI;
		List<Posting> resultsJ;

		if(mComponents.size()<1){
			return result;
		}

		else if(mComponents.size()==1){
			return mComponents.get(0).getPostings(index);

		}

		//variables for traversing our phrase terms
		int iResult=0,jResult=0;
		Posting iPosting,jPosting;
        result = new ArrayList<>();
		resultsI =  mComponents.get(0).getPostings(index);
		for(int i = 1; i<mComponents.size();i++){

			resultsJ = mComponents.get(i).getPostings(index);
            iResult =0;
            jResult=0;
			while((iResult < resultsI.size())  && (jResult < resultsJ.size())){

				iPosting = resultsI.get(iResult);
				jPosting = resultsJ.get(jResult);


				if(iPosting.getDocumentId()<jPosting.getDocumentId()){
					if(result.size() == 0 )
                        result.add(iPosting);
				    else
                    {
                        if(result.get(result.size()-1).getDocumentId()!=iPosting.getDocumentId())
                            result.add(iPosting);

                    }
					iResult++;
				}
				else if(iPosting.getDocumentId()>jPosting.getDocumentId()) {
                    if(result.size() == 0 )
                        result.add(jPosting);
                    else
                    {
                        if(result.get(result.size()-1).getDocumentId()!=jPosting.getDocumentId())
                            result.add(jPosting);

                    }
					jResult++;

				}

				else{

                    if(result.size() == 0 )
                        result.add(jPosting);
                    else
                    {
                        if(result.get(result.size()-1).getDocumentId()!=jPosting.getDocumentId())
                            result.add(jPosting);

                    }

					iResult++;
					jResult++;


				}

			}

			while((iResult < resultsI.size()))
                result.add(resultsI.get(iResult++));

            while((jResult < resultsJ.size()))
                result.add(resultsJ.get(jResult++));




			resultsI = result;
			result = new ArrayList<>();


		}



		return resultsI;

	}
	
	@Override
	public String toString() {
		// Returns a string of the form "[SUBQUERY] + [SUBQUERY] + [SUBQUERY]"
		return "(" +
		 String.join(" + ", mComponents.stream().map(c -> c.toString()).collect(Collectors.toList()))
		 + " )";
	}
}
