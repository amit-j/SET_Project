package cecs429.clustering;

import cecs429.documents.DocumentCorpus;
import cecs429.index.Index;
import cecs429.index.Posting;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

public class ClusterPruningIndex implements Index {


    private Cluster cluster;
    private Index index;


    public void buildIndex(DocumentCorpus corpus, Index index) {
        cluster = new Cluster(corpus, index);
        this.index = index;

    }


    public List<Integer> getPostings(String[] terms) {
        PriorityQueue<QueryResult> results = cluster.query(terms);
        List<Integer> postings = new ArrayList<>();
        for (QueryResult result : results) {
            postings.add(result.documentID);
        }

        return postings;
    }

    @Override
    public List<Posting> getPostings(String term) {
        return null;
    }

    @Override
    public List<String> getVocabulary() {
        return index.getVocabulary();
    }

    @Override
    public List<Posting> getPostingsWithPositions(String term) {
        return null;
    }

    public void writeToDisk(Path path) throws IOException {
        cluster.writeClusterToDisk(path);
    }

    public void readFromDisk(Path path, Index index) throws IOException {
        cluster = new Cluster();
        cluster.readCluster(path, index);
    }
}
