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
    public void testAnd(){
        try {
            Assert.assertEquals(2, testIndexer.executeQuery("dog cat"));
            System.out.println("AND query test case 1 has executed successfully");

            Assert.assertEquals(2, testIndexer.executeQuery("horse monkey elephant"));
            System.out.println("AND query test case 2 has executed successfully");
        } catch (AssertionError e) {
            System.out.println("AND query test case failed "+e.getMessage());
        } catch (Exception e) {
            System.out.println("AND query test case failed "+e.getMessage());
        }
    }

    @org.junit.Test
    public void testOr(){
        try {
            Assert.assertEquals(4,testIndexer.executeQuery("lioness + horse elephant"));
            System.out.println("OR query test case 1 has executed successfully");

            Assert.assertEquals(5,testIndexer.executeQuery("monkey + dog + tiger + elephant + cat"));
            System.out.println("OR query test case 2 has executed successfully");

        } catch (AssertionError e) {
            System.out.println("OR query test case failed "+e.getMessage());
        } catch (Exception e) {
            System.out.println("OR query test case failed "+e.getMessage());
        }
    }

    @org.junit.Test
    public void testNot(){
        try {
            Assert.assertEquals(2,testIndexer.executeQuery("monkey -lion"));
            System.out.println("NOT query test case 1 has executed successfully");

            Assert.assertEquals(2,testIndexer.executeQuery("fish elephant -cat"));
            System.out.println("NOT query test case 2 has executed successfully");

        } catch (AssertionError e) {
            System.out.println("NOT query test case failed "+e.getMessage());
        } catch (Exception e) {
            System.out.println("NOT query test case failed "+e.getMessage());
        }
    }

    @org.junit.Test
    public void testPhrase(){
        try {
            Assert.assertEquals(3,testIndexer.executeQuery("\"elephant dog fish\""));
            System.out.println("Phrase query test case 1 has executed successfully");

            Assert.assertEquals(2,testIndexer.executeQuery("\"tiger&tigress\""));
            System.out.println("Phrase query test case 2 has executed successfully");

        } catch (AssertionError e) {
            System.out.println("Phrase query test case failed "+e.getMessage());
        } catch (Exception e) {
            System.out.println("Phrase query test case failed "+e.getMessage());
        }
    }

    @org.junit.Test
    public void testKGram(){
        try {
            Assert.assertEquals(4,testIndexer.executeQuery("f*h"));
            System.out.println("k-gram query usecase 1 has executed successfully");

            Assert.assertEquals(2,testIndexer.executeQuery("tiger*"));
            System.out.println("k-gram query usecase 2 has executed successfully");

            Assert.assertEquals(2,testIndexer.executeQuery("dog m*k*y"));
            System.out.println("k-gram query usecase 3 has executed successfully");

        } catch (AssertionError e) {
            System.out.println("k-gram query test case failed "+e.getMessage());
        } catch (Exception e) {
            System.out.println("k-gram query test case failed "+e.getMessage());
        }
    }

    @org.junit.Test
    public void testPositions() {
        try {
            List<Posting> p =  testIndexer.getPostings("cat");
            Assert.assertEquals(true,testIndexer.compareLists(new ArrayList<>(Arrays.asList(1, 7)),p.get(1).getPositions()));

            System.out.println("Positions query test case 1 has executed successfully");


            p =  testIndexer.getPostings("eleph");
            Assert.assertEquals(true,testIndexer.compareLists(new ArrayList<>(Arrays.asList(2,4,6,8)),p.get(1).getPositions()));
            System.out.println("Positions query test case 2 has executed successfully");

        } catch (AssertionError e) {
            System.out.println("Positions query test case failed "+e.getMessage());
        } catch (Exception e) {
            System.out.println("Positions query test case failed "+e.getMessage());
        }
    }

}
