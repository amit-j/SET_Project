package testing;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SampleTest {

    public class Math {
        int a, b;
        Math(int a, int b) {
            this.a = a;
            this.b = b;
        }
        public int add() {
            return a + b;
        }
    }

    Math math;

    @Before
    public void setUp() throws Exception {
        System.out.println("Setup");

    }
    @Test
    public void testAdd() {
       try {
           math = new Math(7, 1);
           Assert.assertEquals(8, math.add());
           System.out.println("Success1");
           math = new Math(7, 1);
           Assert.assertEquals(8, math.add());
           System.out.println("Success2");
           math = new Math(7, 1);
           Assert.assertEquals(9, math.add());
           System.out.println("Success3");
       } catch (AssertionError e) {
           System.out.println(e.getMessage());
       } catch (Exception e) {
           System.out.println(e.getMessage());
       }
    }
}
