/**
 * TestRunnerServiceImplTest.java - Test Runner - 2007 Aug 12, 2007 11:47:06 AM - AZ
 */

package org.sakaiproject.testrunner.test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import junit.framework.TestCase;
import junit.framework.TestResult;

import org.sakaiproject.testrunner.TestRunnerService;
import org.sakaiproject.testrunner.impl.TestRunnerServiceImpl;
import org.sakaiproject.testrunner.test.samples.SampleTestFive;
import org.sakaiproject.testrunner.test.samples.SampleTestFour;
import org.sakaiproject.testrunner.test.samples.SampleTestOne;
import org.sakaiproject.testrunner.test.samples.SampleTestThree;
import org.sakaiproject.testrunner.test.samples.SampleTestTwo;
import org.sakaiproject.testrunner.utils.TestRunnerUtils;


/**
 * Class to test the functionality of the test runner service
 *
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class TestRunnerServiceImplTest extends TestCase {

   protected TestRunnerServiceImpl testRunnerService;

   /**
    * integration test, registered, 2 tests (SampleTestOne, SampleTestFive)
    */
   protected final String TESTS_ID1 = "tests-id-1";
   /**
    * load test, registered, 1 test (SampleTestTwo)
    */
   protected final String TESTS_ID2 = "tests-id-2";
   /**
    * load test, registered, 1 tests (SampleTestThree)
    */
   protected final String TESTS_ID3 = "tests-id-3";
   /**
    * integration test, unregistered, (SampleTestFour)
    */
   protected final String TESTS_ID4 = "tests-id-4-unreg";
   protected final String TESTS_ID_INVALID = "tests-id-invalid-xxxxxxxxxxxxx";
   protected final String TESTS_TYPE_INVALID = "tests-type-invalid-xxxxxxxxxxxxx";

   /* (non-Javadoc)
    * @see junit.framework.TestCase#setUp()
    */
   protected void setUp() throws Exception {
      super.setUp();
      testRunnerService = new TestRunnerServiceImpl();

      // simulate registering 3 tests
      Map<String, Set<Class<? extends TestCase>>> testIdMap = 
         new ConcurrentHashMap<String, Set<Class<? extends TestCase>>>();
      Set<Class<? extends TestCase>> test1 = new HashSet<Class<? extends TestCase>>();
      test1.add(SampleTestOne.class);
      test1.add(SampleTestFive.class);
      testIdMap.put(TESTS_ID1, test1);
      Set<Class<? extends TestCase>> test2 = new HashSet<Class<? extends TestCase>>();
      test2.add(SampleTestTwo.class);
      testIdMap.put(TESTS_ID2, test2);
      Set<Class<? extends TestCase>> test3 = new HashSet<Class<? extends TestCase>>();
      test3.add(SampleTestThree.class);
      testIdMap.put(TESTS_ID3, test3);
      testRunnerService.setTestIdMap(testIdMap);

      Map<String, Set<String>> testTypeMap = 
         new ConcurrentHashMap<String, Set<String>>();
      Set<String> typeInt = new HashSet<String>();
      typeInt.add(TESTS_ID1);
      testTypeMap.put(TestRunnerService.TESTING_TESTS_INTEGRATION, typeInt);
      Set<String> typeLoad = new HashSet<String>();
      typeLoad.add(TESTS_ID2);
      typeLoad.add(TESTS_ID3);
      testTypeMap.put(TestRunnerService.TESTING_TESTS_LOAD, typeLoad);
      testRunnerService.setTestTypeMap(testTypeMap);
   }


   /**
    * Test method for {@link org.sakaiproject.testrunner.impl.TestRunnerServiceImpl#getRegisteredTests(String, String)}.
    */
   public void testGetRegisteredTests() {
      List<Class<? extends TestCase>> l = null;

      // test getting normal tests by id
      l = testRunnerService.getRegisteredTests(TESTS_ID1, null);
      assertNotNull(l);
      assertEquals(2, l.size());
      assertTrue(l.contains(SampleTestOne.class));
      assertTrue(l.contains(SampleTestFive.class));

      // test unregistered tests not retrieved
      l = testRunnerService.getRegisteredTests(TESTS_ID4, null);
      assertNotNull(l);
      assertEquals(0, l.size());

      // test getting by type
      l = testRunnerService.getRegisteredTests(null, TestRunnerService.TESTING_TESTS_LOAD);
      assertNotNull(l);
      assertEquals(2, l.size());
      assertTrue(l.contains(SampleTestTwo.class));
      assertTrue(l.contains(SampleTestThree.class));

      l = testRunnerService.getRegisteredTests(null, TestRunnerService.TESTING_TESTS_VALIDATION);
      assertNotNull(l);
      assertEquals(0, l.size());

      // test getting by both where type is right
      l = testRunnerService.getRegisteredTests(TESTS_ID2, TestRunnerService.TESTING_TESTS_LOAD);
      assertNotNull(l);
      assertEquals(1, l.size());
      assertTrue(l.contains(SampleTestTwo.class));

      // test getting by both where type is wrong
      l = testRunnerService.getRegisteredTests(TESTS_ID1, TestRunnerService.TESTING_TESTS_LOAD);
      assertNotNull(l);
      assertEquals(0, l.size());

      // test getting all
      l = testRunnerService.getRegisteredTests(null, null);
      assertNotNull(l);
      assertEquals(4, l.size());
      assertTrue(l.contains(SampleTestOne.class));
      assertTrue(l.contains(SampleTestTwo.class));
      assertTrue(l.contains(SampleTestThree.class));
      assertTrue(l.contains(SampleTestFive.class));


      // test invalid does not fail
      l = testRunnerService.getRegisteredTests(TESTS_ID_INVALID, null);
      assertNotNull(l);
      assertEquals(0, l.size());

      // test invalid type causes failure
      try {
         l = testRunnerService.getRegisteredTests(TESTS_ID1, TESTS_TYPE_INVALID);
         fail("Should not get here");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

   }

   /**
    * Test method for {@link org.sakaiproject.testrunner.impl.TestRunnerServiceImpl#getTestsIds(String)}.
    */
   public void testGetTestsIds() {
      List<String> l = null;

      l = testRunnerService.getTestsIds(null);
      assertNotNull(l);
      assertEquals(3, l.size());
      assertTrue(l.contains(TESTS_ID1));
      assertTrue(l.contains(TESTS_ID2));
      assertTrue(l.contains(TESTS_ID3));

      l = testRunnerService.getTestsIds(TestRunnerService.TESTING_TESTS_LOAD);
      assertNotNull(l);
      assertEquals(2, l.size());
      assertTrue(l.contains(TESTS_ID2));
      assertTrue(l.contains(TESTS_ID3));

      l = testRunnerService.getTestsIds(TestRunnerService.TESTING_TESTS_VALIDATION);
      assertNotNull(l);
      assertEquals(0, l.size());
   }

   /**
    * Test method for {@link TestRunnerServiceImpl#registerTest(String, String, Class)}
    */
   @SuppressWarnings("unchecked")
   public void testRegisterTests() {
      List<Class<? extends TestCase>> l = null;

      l = testRunnerService.getRegisteredTests(TESTS_ID4, null);
      assertNotNull(l);
      assertEquals(0, l.size());

      // test registering a test
      testRunnerService.registerTests(TESTS_ID4, TestRunnerService.TESTING_TESTS_INTEGRATION, new Class[] {SampleTestFive.class});
      l = testRunnerService.getRegisteredTests(TESTS_ID4, null);
      assertNotNull(l);
      assertEquals(1, l.size());
      assertTrue(l.contains(SampleTestFive.class));
      assertFalse(l.contains(SampleTestOne.class));

      // test registering another test under this id
      testRunnerService.registerTest(TESTS_ID4, TestRunnerService.TESTING_TESTS_INTEGRATION, SampleTestOne.class);
      l = testRunnerService.getRegisteredTests(TESTS_ID4, TestRunnerService.TESTING_TESTS_INTEGRATION);
      assertNotNull(l);
      assertEquals(2, l.size());
      assertTrue(l.contains(SampleTestFive.class));
      assertTrue(l.contains(SampleTestOne.class));

      // test changing the type of all these tests
      testRunnerService.registerTest(TESTS_ID4, TestRunnerService.TESTING_TESTS_VALIDATION, SampleTestOne.class);
      l = testRunnerService.getRegisteredTests(TESTS_ID4, TestRunnerService.TESTING_TESTS_VALIDATION);
      assertNotNull(l);
      assertEquals(2, l.size());
      assertTrue(l.contains(SampleTestFive.class));
      assertTrue(l.contains(SampleTestOne.class));
   }

   /**
    * Test method for {@link org.sakaiproject.testrunner.impl.TestRunnerServiceImpl#unregisterTests(String, String)}.
    */
   public void testUnregisterTests() {
      List<Class<? extends TestCase>> l = null;

      // unregister by id
      l = testRunnerService.getRegisteredTests(TESTS_ID1, null);
      assertNotNull(l);
      assertEquals(2, l.size());

      testRunnerService.unregisterTests(TESTS_ID1, null);
      l = testRunnerService.getRegisteredTests(TESTS_ID1, null);
      assertNotNull(l);
      assertEquals(0, l.size());

      // unregister by type
      l = testRunnerService.getRegisteredTests(null, TestRunnerService.TESTING_TESTS_LOAD);
      assertNotNull(l);
      assertEquals(2, l.size());

      testRunnerService.unregisterTests(null, TestRunnerService.TESTING_TESTS_LOAD);
      l = testRunnerService.getRegisteredTests(null, TestRunnerService.TESTING_TESTS_LOAD);
      assertNotNull(l);
      assertEquals(0, l.size());

      // check that unregistering invalid is ok
      testRunnerService.unregisterTests(TESTS_ID_INVALID, TESTS_TYPE_INVALID);
   }

   /**
    * Test method for {@link org.sakaiproject.testrunner.impl.TestRunnerServiceImpl#runTests(java.lang.String)}.
    */
   public void testRunTests() {
      TestResult t = null;
      Map<Class<? extends TestCase>, TestResult> m = null;

      // turn on all tests
      testRunnerService.setEnabled(true);
      testRunnerService.setTestIntegration(true);
      testRunnerService.setTestLoad(true);
      testRunnerService.setTestValidity(true);
      
      // make sure running test for invalid ID is ok
      m = testRunnerService.runTests(TESTS_ID_INVALID, null);
      assertNotNull(m);
      assertEquals(0, m.size());
      assertFalse(TestRunnerUtils.checkTestsSuccess(m));

      // test invalid type causes failure
      try {
         m = testRunnerService.runTests(null, TESTS_TYPE_INVALID);
         fail("Should not get here");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

      // test running passing tests
      m = testRunnerService.runTests(TESTS_ID1, null);
      assertNotNull(m);
      assertEquals(2, m.size());
      t = m.get(SampleTestOne.class);
      assertNotNull(t);
      assertTrue(t.wasSuccessful());
      assertEquals(3, t.runCount()); // ran 3 tests
      assertTrue(TestRunnerUtils.checkTestsSuccess(m));

      // test running tests by type
      m = testRunnerService.runTests(null, TestRunnerService.TESTING_TESTS_INTEGRATION);
      assertNotNull(m);
      assertEquals(2, m.size());
      t = m.get(SampleTestOne.class);
      assertNotNull(t);
      assertTrue(t.wasSuccessful());
      assertEquals(3, t.runCount()); // ran 3 tests
      assertTrue(TestRunnerUtils.checkTestsSuccess(m));

      m = testRunnerService.runTests(null, TestRunnerService.TESTING_TESTS_LOAD);
      assertNotNull(m);
      assertEquals(2, m.size());
      t = m.get(SampleTestTwo.class);
      assertNotNull(t);
      assertFalse(t.wasSuccessful());
      assertEquals(2, t.runCount());
      assertFalse(TestRunnerUtils.checkTestsSuccess(m));

      // test running failing test
      m = testRunnerService.runTests(TESTS_ID2, null);
      assertNotNull(m);
      assertEquals(1, m.size());
      t = m.get(SampleTestTwo.class);
      assertNotNull(t);
      assertFalse(t.wasSuccessful());
      assertEquals(2, t.runCount());
      assertFalse(TestRunnerUtils.checkTestsSuccess(m));

      // test running error test
      m = testRunnerService.runTests(TESTS_ID3, null);
      assertNotNull(m);
      assertEquals(1, m.size());
      t = m.get(SampleTestThree.class);
      assertNotNull(t);
      assertFalse(t.wasSuccessful());
      assertEquals(2, t.runCount());
      assertFalse(TestRunnerUtils.checkTestsSuccess(m));

      // test running multiple tests (passing and failing)
      testRunnerService.registerTest(TESTS_ID4, TestRunnerService.TESTING_TESTS_VALIDATION, SampleTestFour.class);
      testRunnerService.registerTest(TESTS_ID4, TestRunnerService.TESTING_TESTS_VALIDATION, SampleTestTwo.class);
      m = testRunnerService.runTests(TESTS_ID4, null);
      assertNotNull(m);
      assertEquals(2, m.size());
      t = m.get(SampleTestFour.class);
      assertNotNull(t);
      assertTrue(t.wasSuccessful());
      t = m.get(SampleTestTwo.class);
      assertNotNull(t);
      assertFalse(t.wasSuccessful());
      assertFalse(TestRunnerUtils.checkTestsSuccess(m));

      // test running all tests (passing and failing)
      m = testRunnerService.runTests(null, null);
      assertNotNull(m);
      assertEquals(5, m.size());
      t = m.get(SampleTestOne.class);
      assertNotNull(t);
      assertTrue(t.wasSuccessful());
      t = m.get(SampleTestTwo.class);
      assertNotNull(t);
      assertFalse(t.wasSuccessful());
      assertFalse(TestRunnerUtils.checkTestsSuccess(m));

      // turn off some tests
      testRunnerService.setTestLoad(false);

      // check integration tests still ok
      m = testRunnerService.runTests(TESTS_ID1, null);
      assertNotNull(m);
      assertEquals(2, m.size());

      // check load tests cannot be run
      m = testRunnerService.runTests(null, TestRunnerService.TESTING_TESTS_LOAD);
      assertNotNull(m);
      assertEquals(0, m.size());

      // check some tests can still be run
      m = testRunnerService.runTests(null, null);
      assertNotNull(m);
      assertEquals(3, m.size());
      
      // turn off all tests
      testRunnerService.setEnabled(false);

      // test no tests run
      m = testRunnerService.runTests(TESTS_ID1, null);
      assertNotNull(m);
      assertEquals(0, m.size());

      m = testRunnerService.runTests(null, null);
      assertNotNull(m);
      assertEquals(0, m.size());
   }

   public void testGetTestRunnerParam() {
      assertTrue(testRunnerService.isTestingEnabled());
      assertTrue(testRunnerService.getTestRunnerParam(TestRunnerService.TESTING_TESTS_INTEGRATION));
      assertFalse(testRunnerService.getTestRunnerParam(TestRunnerService.TESTING_TESTS_LOAD));
      assertFalse(testRunnerService.getTestRunnerParam(TestRunnerService.TESTING_TESTS_VALIDATION));
   }

   public void testSetTestRunnerParam() {
      assertTrue(testRunnerService.isTestingEnabled());
      assertTrue(testRunnerService.getTestRunnerParam(TestRunnerService.TESTING_TESTS_INTEGRATION));
      assertFalse(testRunnerService.getTestRunnerParam(TestRunnerService.TESTING_TESTS_LOAD));

      testRunnerService.setTestRunnerParam(TestRunnerService.TESTING_TESTS_LOAD, true);
      assertTrue(testRunnerService.getTestRunnerParam(TestRunnerService.TESTING_TESTS_LOAD));

      testRunnerService.setTestingEnabled(false);
      assertFalse(testRunnerService.isTestingEnabled());
      assertFalse(testRunnerService.getTestRunnerParam(TestRunnerService.TESTING_TESTS_INTEGRATION));
      assertFalse(testRunnerService.getTestRunnerParam(TestRunnerService.TESTING_TESTS_LOAD));
   }

   public void testGetTestsTypes() {
      List<String> l = testRunnerService.getTestsTypes();
      assertNotNull(l);
      assertEquals(2, l.size());
      assertTrue(l.contains(TestRunnerService.TESTING_TESTS_INTEGRATION));
      assertTrue(l.contains(TestRunnerService.TESTING_TESTS_LOAD));
      assertFalse(l.contains(TestRunnerService.TESTING_TESTS_VALIDATION));

      testRunnerService.registerTest(TESTS_ID4, TestRunnerService.TESTING_TESTS_VALIDATION, SampleTestFour.class);

      l = testRunnerService.getTestsTypes();
      assertNotNull(l);
      assertEquals(3, l.size());
      assertTrue(l.contains(TestRunnerService.TESTING_TESTS_INTEGRATION));
      assertTrue(l.contains(TestRunnerService.TESTING_TESTS_LOAD));
      assertTrue(l.contains(TestRunnerService.TESTING_TESTS_VALIDATION));
   }

   public void testGetTestTypeForTestsId() {
      assertEquals(testRunnerService.getTestTypeForTestsId(TESTS_ID1), TestRunnerService.TESTING_TESTS_INTEGRATION);
      assertEquals(testRunnerService.getTestTypeForTestsId(TESTS_ID2), TestRunnerService.TESTING_TESTS_LOAD);
      assertEquals(testRunnerService.getTestTypeForTestsId(TESTS_ID3), TestRunnerService.TESTING_TESTS_LOAD);

      assertNull(testRunnerService.getTestTypeForTestsId("invalid id 3333"));
   }

}
