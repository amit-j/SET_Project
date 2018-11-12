package cecs429.query;

import cecs429.text.BetterTokenProcessor;
import cecs429.text.TokenProcessor;

import java.util.ArrayList;
import java.util.List;

public class RankedRetrievalParser {

    public String[] parseQuery(String query, TokenProcessor tokenProcessor){

        List<String> termsList = new ArrayList<>();

        String[] tokens = query.split(" ");
        for (String token : tokens){
            String[] terms = tokenProcessor.processToken(token);
            for (String term : terms) {
                termsList.add(term);
            }
        }

         return termsList.toArray(new String[termsList.size()]);

    }

    public static void main(String[] arg){
        BetterTokenProcessor processor = new BetterTokenProcessor();
        RankedRetrievalParser parser = new RankedRetrievalParser();
        String[] temp = parser.parseQuery("wrapper namrata--lomte", processor);
        for (String term: temp) {
            System.out.println(term);
        }
    }
}
