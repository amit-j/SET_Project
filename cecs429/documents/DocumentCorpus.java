package cecs429.documents;

/**
 * Represents a collection of jsonDocuments used to build an index.
 */
public interface DocumentCorpus {
	/**
	 * Gets all jsonDocuments in the corpus.
	 */
	Iterable<Document> getDocuments();
	
	/**
	 * The number of jsonDocuments in the corpus.
	 */
	int getCorpusSize();
	
	/**
	 * Returns the document with the given document ID.
	 */
	Document getDocument(int id);
}
