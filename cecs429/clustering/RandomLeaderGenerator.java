package cecs429.clustering;

import java.util.HashSet;
import java.util.Random;

public class RandomLeaderGenerator {
    Random random;
    HashSet<Integer> generatedLeaders;
    int corpusSize;

    public RandomLeaderGenerator(int max) {
        corpusSize = max;
        random = new Random();
        generatedLeaders = new HashSet<>();
        System.out.println("Random seed :" + random);
    }

    public Integer getNextLeader() {
        int l = random.nextInt(corpusSize);

        while (generatedLeaders.contains(l))
            l = random.nextInt(corpusSize);

        generatedLeaders.add(l);

        return l;
    }
}
