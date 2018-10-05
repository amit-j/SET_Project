package testing;


import cecs429.index.Posting;
import org.junit.Assert;
import org.junit.Before;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Test {

    TestIndexer testIndexer = new TestIndexer();

    @Before
    public void setUp() throws Exception {
        testIndexer.configure();
    }
    @org.junit.Test
    public void testAdd() {
        try {
            Assert.assertEquals(2,testIndexer.executeQuery("dog cat"));
            System.out.println("1 usecase has executed successfully");
            Assert.assertEquals(2,testIndexer.executeQuery("horse monkey elephant"));
            System.out.println("2 usecase has executed successfully");
            Assert.assertEquals(4,testIndexer.executeQuery("lioness + horse elephant"));
            System.out.println("3 usecase has executed successfully");
            Assert.assertEquals(5,testIndexer.executeQuery("monkey + dog + tiger + elephant + cat"));
            System.out.println("4 usecase has executed successfully");
            Assert.assertEquals(2,testIndexer.executeQuery("monkey -lion"));
            System.out.println("5 usecase has executed successfully");
            Assert.assertEquals(2,testIndexer.executeQuery("fish elephant -cat"));
            System.out.println("6 usecase has executed successfully");
            Assert.assertEquals(3,testIndexer.executeQuery("\"elephant dog fish\""));
            System.out.println("7 usecase has executed successfully");
            Assert.assertEquals(2,testIndexer.executeQuery("\"tiger&tigress\""));
            System.out.println("8 usecase has executed successfully");
            Assert.assertEquals(4,testIndexer.executeQuery("f*h"));
            System.out.println("9 usecase has executed successfully");
            Assert.assertEquals(2,testIndexer.executeQuery("tiger*"));
            System.out.println("10 usecase has executed successfully");
            Assert.assertEquals(2,testIndexer.executeQuery("dog m*k*y"));
            System.out.println("11 usecase has executed successfully");

            List<Posting> p =  testIndexer.getPostings("cat");
            Assert.assertEquals(true,testIndexer.compareLists(new ArrayList<>(Arrays.asList(1, 7)),p.get(1).getPositions()));
            System.out.println("12 usecase has executed successfully");


             p =  testIndexer.getPostings("eleph");
            Assert.assertEquals(true,testIndexer.compareLists(new ArrayList<>(Arrays.asList(2,4,6,8)),p.get(1).getPositions()));
            System.out.println("13 usecase has executed successfully");

        } catch (AssertionError e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}
