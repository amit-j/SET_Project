package cecs429.text;

import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

public class BetterTokenProcessor implements TokenProcessor{

    // removed implementation of TokenProcessor
    @Override
    public String[] processToken(String token) {

        SnowballStemmer snowballStemmer = new englishStemmer();

        String processedTokens[];
        token = token.replaceAll("^[^a-zA-Z0-9\\s]+|[^a-zA-Z0-9\\s]+$+|\'+|\"",  " ").toLowerCase();

        if(token.contains("-")){
            String[] temp = token.split("-");
            processedTokens = new String[temp.length+1];
            processedTokens[0] = token.replace("-","");
            for (int i=1; i<temp.length+1; i++){

                snowballStemmer.setCurrent(temp[i-1]);
                snowballStemmer.stem();
                String result = snowballStemmer.getCurrent();


                processedTokens[i] = result;
            }

        } else {
           processedTokens = new String[1];

            snowballStemmer.setCurrent(token);
            snowballStemmer.stem();
            String result = snowballStemmer.getCurrent();
            processedTokens[0] = result;
        }

        return processedTokens;
    }

    public static void main(String arg[]){

        BetterTokenProcessor processor = new BetterTokenProcessor();
        String[] temp = processor.processToken("@Namrata'-Lomte10'");
        for (int i=0; i<temp.length; i++){
            System.out.println(temp[i]);
        }
    }
}
