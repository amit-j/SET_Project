package cecs429.text;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 * A BasicTokenProcessor creates terms from tokens by removing all non-alphanumeric characters from the token, and
 * converting it to all lowercase.
 */
public class BasicTokenProcessor implements TokenProcessor {


    Set<String> unStemmedVocabs;

    public BasicTokenProcessor() {
        unStemmedVocabs = new TreeSet<>();
    }

    @Override
    public String[] processToken(String token) {
        String[] output = {token.replaceAll("\\W", "").toLowerCase()};
        Collections.addAll(unStemmedVocabs, output);
        return output;
    }

    public Set<String> getUnStemmedVocabs() {
        return unStemmedVocabs;
    }
}
