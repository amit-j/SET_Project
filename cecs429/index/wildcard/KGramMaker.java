package cecs429.index.wildcard;


import java.util.ArrayList;
import java.util.List;

public class KGramMaker {

    private final int mKGramCount = 3;

    //This is an composite class to help with creation of kgrams
    public List<String> makeKgrams(String term) {


        int currentNGram = 1;
        List<String> kgramList = new ArrayList();
        term = "$" + term + "$";

        while (currentNGram <= mKGramCount) {
            int startPos = 0;
            //reference : https://stackoverflow.com/questions/3760152/split-string-to-equal-length-substrings-in-java : Alan Moore
                /*for(String kgram:term.split("(?<=\\G.{"+currentNGram+"})")){
                    kgramList.add(kgram);
                }
                currentNGram++;*/

            while (startPos + currentNGram <= term.length()) {

                kgramList.add(term.substring(startPos, startPos + currentNGram));
                startPos++;
            }
            currentNGram++;
        }

        return kgramList;
    }

}
