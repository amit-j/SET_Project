package cecs429.index;

import java.util.List;

/**
 * An Index can retrieve postings for a term from a data structure associating terms and the jsonDocuments
 * that contain them.
 */
public interface Index {
    /**
     * Retrieves a list of Postings of jsonDocuments that contain the given term.
     */
    List<Posting> getPostings(String term);

    /**
     * A (sorted) list of all terms in the index vocabulary.
     */
    List<String> getVocabulary();

    List<Posting> getPostingsWithPositions(String term);


}
