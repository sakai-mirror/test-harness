/**
 * TestRunnerUtilsTest.java - Test Runner - 2007 Aug 12, 2007 3:38:24 PM - AZ
 */

package org.sakaiproject.testrunner.test;

import java.util.HashMap;
import java.util.List;

import org.sakaiproject.testrunner.test.samples.SampleTestOne;
import org.sakaiproject.testrunner.test.samples.SampleTestThree;
import org.sakaiproject.testrunner.test.samples.SampleTestTwo;
import org.sakaiproject.testrunner.utils.TestRunnerUtils;

import junit.framework.TestCase;
import junit.framework.TestResult;


/**
 * Testing the utils
 *
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class TestRunnerUtilsTest extends TestCase {

   /**
    * Test method for {@link org.sakaiproject.testrunner.utils.TestRunnerUtils#doTestRun(java.lang.Class)}.
    */
   public void testDoTestRun() {
      TestResult t = null;

      t = TestRunnerUtils.doTestRun(SampleTestOne.class);
      assertNotNull(t);
      assertTrue(t.wasSuccessful());
      assertEquals(3, t.runCount()); // ran 3 tests

      t = TestRunnerUtils.doTestRun(SampleTestTwo.class);
      assertNotNull(t);
      assertFalse(t.wasSuccessful());
      assertEquals(2, t.runCount());
   }

   /**
    * Test method for {@link org.sakaiproject.testrunner.utils.TestRunnerUtils#checkTestsSuccess(java.util.Map)}.
    */
   public void testCheckTestsSuccess() {
      assertFalse( TestRunnerUtils.checkTestsSuccess(new HashMap<Class<? extends TestCase>, TestResult>()) );
      // TODO - add better tests
   }

   /**
    * Test method for {@link org.sakaiproject.testrunner.utils.TestRunnerUtils#summarizeTestsResults(java.util.Map)}.
    */
   public void testSummarizeTestsResults() {
      List<String> l = null;

      l = TestRunnerUtils.summarizeTestsResults(new HashMap<Class<? extends TestCase>, TestResult>());
      assertNotNull(l);
      assertTrue(l.isEmpty());

      // TODO - add better tests
   }

   /**
    * Test method for {@link org.sakaiproject.testrunner.utils.TestRunnerUtils#detailedTestResult(junit.framework.TestResult)}.
    */
   public void testDetailedTestResult() {
      TestResult result = null;
      List<String> l = null;

      result = TestRunnerUtils.doTestRun(SampleTestOne.class);
      l = TestRunnerUtils.detailedTestResult(result);
      assertNotNull(l);
      assertFalse(l.isEmpty());
   }

   /**
    * Test method for {@link org.sakaiproject.testrunner.utils.TestRunnerUtils#makeSummary(junit.framework.TestResult)}.
    */
   public void testMakeSummary() {
      TestResult result = null;
      String summary = null;

      result = TestRunnerUtils.doTestRun(SampleTestOne.class);
      summary = TestRunnerUtils.makeSummary(result);
      assertNotNull(summary);
   }

   /**
    * Test method for {@link org.sakaiproject.testrunner.utils.TestRunnerUtils#executeTests(java.lang.String)}.
    */
   public void testExecuteTests() {
      // NOTE: all these tests are without having a live application context
      // execute test that passes
      TestRunnerUtils.executeSpringTests(SampleTestOne.class, null);

      // execute test that fails
      try {
         TestRunnerUtils.executeSpringTests(SampleTestTwo.class, null);
         fail("Should have thrown exception");
      } catch (RuntimeException e) {
         assertNotNull(e);
      }

      // execute test that errors out
      try {
         TestRunnerUtils.executeSpringTests(SampleTestThree.class, null);
         fail("Should have thrown exception");
      } catch (RuntimeException e) {
         assertNotNull(e);
      }
   }

}
