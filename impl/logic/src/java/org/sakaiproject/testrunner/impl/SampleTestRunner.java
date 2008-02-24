/**
 * SampleTestRunner.java - Test Runner - 2007 Aug 13, 2007 6:54:02 PM - AZ
 */

package org.sakaiproject.testrunner.impl;

import java.util.Map;

import junit.framework.TestCase;
import junit.framework.TestResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.testrunner.TestRunnerService;
import org.sakaiproject.testrunner.impl.tests.SampleTestSakaiUser;
import org.sakaiproject.testrunner.impl.tests.SimpleTest;
import org.sakaiproject.testrunner.utils.TestRunnerUtils;


/**
 * This demonstrates running tests from within a service,
 * you can run tests from within your services by writing code
 * similar to this, you just need to inject the test runner service
 * and register your tests and then run them
 *
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class SampleTestRunner {

   private final static Log log = LogFactory.getLog(SampleTestRunner.class);

   private TestRunnerService testRunnerService;
   public void setTestRunnerService(TestRunnerService testRunnerService) {
      this.testRunnerService = testRunnerService;
   }

   private String myTestsId = "aaronz";

   public void init() {
      // register some tests
      testRunnerService.registerTest(myTestsId, TestRunnerService.TESTING_TESTS_INTEGRATION, SimpleTest.class);
      testRunnerService.registerTest(myTestsId, TestRunnerService.TESTING_TESTS_INTEGRATION, SampleTestSakaiUser.class);

      // call a method to run the tests
      if (runMyTests()) {
         log.info("My Sample tests running within a service passed!!");
      }

      // unregister my tests since I don't need them anymore
      testRunnerService.unregisterTests(myTestsId, null);
   }

   /**
    * This runs my tests and returns a boolean which indicates if they passed or failed,
    * you would probably want to do something useful with the output
    * @return true if tests passed, false otherwise
    */
   private boolean runMyTests() {
      Map<Class<? extends TestCase>, TestResult> m = testRunnerService.runTests(myTestsId, null);
      return TestRunnerUtils.checkTestsSuccess(m);
   }

}
