package cecs429.clustering;

import cecs429.classifier.DocumentVector;
import cecs429.documents.DocumentCorpus;
import cecs429.index.Index;
import cecs429.index.Posting;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.IntStream;

public class Cluster {


    private List<String> vocab;

    private HashMap<String, Integer> vectorSpace;
    private HashMap<Integer, DocumentVector> leaderVectors;
    private HashMap<Integer, DocumentVector> followers;
    private HashMap<Integer, Double> docuementWeights;
    HashMap<Integer, List<Integer>> cluster;


    public Cluster(){

        vectorSpace =new HashMap<>();
        followers = new HashMap<>();
        leaderVectors = new HashMap<>();
        docuementWeights = new HashMap<>();
        cluster = new HashMap<>();
    }

    public Cluster(DocumentCorpus corpus, Index index) {


        docuementWeights = new HashMap<>();
        getNewLeaders(corpus, index);

        //we ensure we have "good" leaders by checking if the cluster has atleast 70% leader coverage
        //by reselecting the leaders again! not really efficient but we only plan on doing this once!
        while (cluster.size() < (int) 0.7 * Math.sqrt(corpus.getCorpusSize()))
            getNewLeaders(corpus, index);


    }


    private void getNewLeaders(DocumentCorpus corpus, Index index) {
        int leaderCount = (int) Math.sqrt(corpus.getCorpusSize());

        HashSet<Integer> leaders = new HashSet<>();
        RandomLeaderGenerator generator = new RandomLeaderGenerator(corpus.getCorpusSize());

        while (leaders.size() < leaderCount) {
            int l = generator.getNextLeader();
            leaders.add(l);
        }
        vocab = index.getVocabulary();
        vectorSpace = new HashMap<>();
        int termID = 0;
        for (String term : vocab) {
            vectorSpace.put(term, termID++);
        }

        leaderVectors = new HashMap<>();
        followers = new HashMap<>();

        for (String term : vocab) {
            for (Posting p : index.getPostingsWithPositions(term)) {
                double wdt = 1 + Math.log(p.getPositions().size());

                if (leaders.contains(p.getDocumentId())) {

                    if (leaderVectors.containsKey(p.getDocumentId())) {

                        DocumentVector vector = leaderVectors.get(p.getDocumentId());
                        vector.addWdt(wdt, vectorSpace.get(term));
                        leaderVectors.put(p.getDocumentId(), vector);
                    } else {

                        DocumentVector vector = new DocumentVector(p.getDocumentId());
                        vector.addWdt(wdt, vectorSpace.get(term));
                        leaderVectors.put(p.getDocumentId(), vector);

                    }


                } else {

                    if (followers.containsKey(p.getDocumentId())) {

                        DocumentVector vector = followers.get(p.getDocumentId());
                        vector.addWdt(wdt, vectorSpace.get(term));
                        followers.put(p.getDocumentId(), vector);
                    } else {

                        DocumentVector vector = new DocumentVector(p.getDocumentId());
                        vector.addWdt(wdt, vectorSpace.get(term));
                        followers.put(p.getDocumentId(), vector);

                    }

                }

                if (docuementWeights.containsKey(p.getDocumentId())) {
                    Double weight = docuementWeights.get(p.getDocumentId());
                    weight += wdt * wdt;
                    docuementWeights.put(p.getDocumentId(), weight);
                } else {

                    Double weight = new Double(0);
                    weight += wdt * wdt;
                    docuementWeights.put(p.getDocumentId(), weight);
                }
            }

        }


        cluster = clusterFollowersToLeaders();
    }


    public void writeClusterToDisk(Path path) throws IOException {
        FileOutputStream fos = new FileOutputStream(path.toAbsolutePath().toString() + "\\index\\cluster.bin");
        DataOutputStream stream = new DataOutputStream(fos);

        for (Integer leader : cluster.keySet()) {
            stream.writeInt(leader);
            stream.writeInt(cluster.get(leader).size());
            for (Integer fol : cluster.get(leader)) {
                stream.writeInt(fol);
            }
        }

        stream.close();
    }


    public void readCluster(Path path, Index index) throws IOException {
        FileInputStream fin = new FileInputStream(path.toAbsolutePath().toString() + "\\index\\cluster.bin");
        DataInputStream stream = new DataInputStream(fin);
        vocab = index.getVocabulary();
        while (stream.available() > 0) {

            int leader = stream.readInt();
            int numFol = stream.readInt();
            List<Integer> arrayList = new ArrayList<>();
            while (numFol > 0) {
                arrayList.add(stream.readInt());
                numFol--;
            }
            cluster.put(leader, arrayList);
        }

        int termID = 0;
        for (String term : vocab) {
            vectorSpace.put(term, termID++);
        }


        for (String term : vocab) {
            for (Posting p : index.getPostingsWithPositions(term)) {
                double wdt = 1 + Math.log(p.getPositions().size());

                if (cluster.containsKey(p.getDocumentId())) {

                    if (leaderVectors.containsKey(p.getDocumentId())) {

                        DocumentVector vector = leaderVectors.get(p.getDocumentId());
                        vector.addWdt(wdt, vectorSpace.get(term));
                        leaderVectors.put(p.getDocumentId(), vector);
                    } else {

                        DocumentVector vector = new DocumentVector(p.getDocumentId());
                        vector.addWdt(wdt, vectorSpace.get(term));
                        leaderVectors.put(p.getDocumentId(), vector);

                    }


                } else {

                    if (followers.containsKey(p.getDocumentId())) {

                        DocumentVector vector = followers.get(p.getDocumentId());
                        vector.addWdt(wdt, vectorSpace.get(term));
                        followers.put(p.getDocumentId(), vector);
                    } else {

                        DocumentVector vector = new DocumentVector(p.getDocumentId());
                        vector.addWdt(wdt, vectorSpace.get(term));
                        followers.put(p.getDocumentId(), vector);

                    }

                }

                if (docuementWeights.containsKey(p.getDocumentId())) {
                    Double weight = docuementWeights.get(p.getDocumentId());
                    weight += wdt * wdt;
                    docuementWeights.put(p.getDocumentId(), weight);
                } else {

                    Double weight = new Double(0);
                    weight += wdt * wdt;
                    docuementWeights.put(p.getDocumentId(), weight);
                }
            }

        }


        stream.close();

    }

    /* gets N random numbers */
    private IntStream getRandomNumbers(int n) {
        Random r = new Random();
        return IntStream.generate(r::nextInt).limit(n);
    }


    private HashMap<Integer, List<Integer>> clusterFollowersToLeaders() {
        HashMap<Integer, List<Integer>> leaderFollowerMatrix = new HashMap<>();
        for (DocumentVector follower : followers.values()) {
            double minDist = Double.MAX_VALUE;
            int leaderID = -1;
            for (DocumentVector leader : leaderVectors.values()) {

                double sim = leader.findSimilarity(follower);
                if (sim < minDist) {
                    leaderID = leader.getDocumentID();
                    minDist = sim;
                }

            }

            if (leaderFollowerMatrix.containsKey(leaderID)) {
                List<Integer> followers = leaderFollowerMatrix.get(leaderID);
                followers.add(follower.getDocumentID());
                leaderFollowerMatrix.put(leaderID, followers);
            } else {
                ArrayList<Integer> followers = new ArrayList<>();
                followers.add(follower.getDocumentID());
                leaderFollowerMatrix.put(leaderID, followers);
            }

        }
        return leaderFollowerMatrix;
    }


    public PriorityQueue<QueryResult> query(String[] terms) {


        DocumentVector queryVector = createQueryVector(terms);
        double minDist = Double.MAX_VALUE;
        int leaderID = -1;
        for (Integer docID : leaderVectors.keySet()) {
            DocumentVector leader = leaderVectors.get(docID);

            //since we are not enforcing equal distribution of followers among the leaders
            if (!cluster.containsKey(leader.getDocumentID()))
                continue;

            double dist = leader.calculateDistance(queryVector);
            if (dist < minDist) {
                minDist = dist;
                leaderID = leader.getDocumentID();
            }

        }

        PriorityQueue<QueryResult> results = new PriorityQueue<>(50, (q1, q2) -> Double.compare(q1.dist, q2.dist));

        for (Integer followerID : cluster.get(leaderID)) {

            DocumentVector follower = followers.get(followerID);
            double dist = follower.calculateDistance(queryVector);


            results.offer(new QueryResult(followerID, dist));
            if (results.size() > 50)
                results.poll();


        }

        return results;

    }


    private DocumentVector createQueryVector(String[] terms) {

        DocumentVector vector = new DocumentVector(-1);
        HashMap<String, Integer> termFreqmap = new HashMap<>();
        for (String term : terms) {

            if (termFreqmap.containsKey(term))
                termFreqmap.put(term, termFreqmap.get(term) + 1);
            else
                termFreqmap.put(term, 1);
        }


        for (String term : termFreqmap.keySet()) {

            int freq = termFreqmap.get(term);
            double wdt = 1 + Math.log(freq);
            vector.addWdt(wdt, vectorSpace.containsKey(term)?vectorSpace.get(term):-1);
        }
        return vector;
    }


}

class QueryResult {
    int documentID;
    double dist;

    public QueryResult(int docID, double dst) {

        documentID = docID;
        dist = dst;

    }
}