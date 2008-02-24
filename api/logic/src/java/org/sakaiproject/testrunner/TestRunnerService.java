/**
 * TestRunnerService.java - Test Runner - 2007 Aug 11, 2007 9:36:51 PM - AZ
 */

package org.sakaiproject.testrunner;

import java.util.List;
import java.util.Map;

import org.sakaiproject.testrunner.utils.TestRunnerUtils;

import junit.framework.TestCase;
import junit.framework.TestResult;


/**
 * Programatic integration test running service<br/>
 * This allows the running of tests from inside java framework code<br/>
 * <br/>
 * Using this service is easy and very flexible. The simplest option is to 
 * use the {@link TestExecutor} to register or run tests from a spring bean config.
 * See the javadocs for that class for more details.<br/>
 * 
 * You can also register and run your tests from within your service. 
 * Just inject an instance of the {@link TestRunnerService} into the code
 * which you want to use to run a bunch of tests. Use the methods in the class to 
 * register your test classes and run the tests. Use the {@link TestRunnerUtils}
 * to turn the {@link TestResult} objects into easy to read results.<br/>
 * <br/>
 * You can easily configure this service from sakai.properties. 
 * Here is an example of settings which enable the testing, turn on
 * integration and load tests, disable validation tests, and disable automatic test runs:
 * <xmp># TestRunner settings
   enabled@org.sakaiproject.testrunner.TestRunnerService = true
   testIntegration@org.sakaiproject.testrunner.TestRunnerService = true
   testLoad@org.sakaiproject.testrunner.TestRunnerService = true
   testValidity@org.sakaiproject.testrunner.TestRunnerService = false
   testDelaySeconds@org.sakaiproject.testrunner.TestRunnerService = -1
   </xmp>
 *
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface TestRunnerService {

   /**
    * Run integration tests, default: true
    */
   public final static String TESTING_TESTS_INTEGRATION = "testrunner.integration";
   /**
    * Run load tests (these are very costly and should not be run in production, 
    * they should be run to benchmark your system or check general performance of a new setup), default: false
    */
   public final static String TESTING_TESTS_LOAD = "testrunner.load";
   /**
    * Run data validation tests (these are very costly and should not be run normally,
    * they should be run after migrations or upgrades to ensure data is valid), default:false
    */
   public final static String TESTING_TESTS_VALIDATION = "testrunner.validation";

   /**
    * Gets the unique test ids for all currently registered tests
    * @param testsType (optional) the type of test (e.g. integration, load, etc.), 
    * this will limit the test ids returned to a specific type 
    * (use TESTING_TESTS_XXXX constants), null will match any type of test
    * @return all currently registered test ids
    */
   public List<String> getTestsIds(String testsType);

   /**
    * Gets the test types that are used
    * @return a list of all current test types (e.g. integration, load, etc.)
    */
   public List<String> getTestsTypes();

   /**
    * Get the test type for a specific test
    * @param testsId a unique id for a set of tests (could be a userId or sessionId or whatever)
    * @return the type of test (e.g. integration, load, etc.), null if not type can be found (id is invalid)
    */
   public String getTestTypeForTestsId(String testsId);

   /**
    * Gets all the currently registered tests for a specific test id
    * @param testsId a unique id for a set of tests (could be a userId or sessionId or whatever)
    * @param testsType (optional) the type of test (e.g. integration, load, etc.), 
    * this will limit the test ids returned to a specific type 
    * (use TESTING_TESTS_XXXX constants), null will match any type of test
    * @return a list of all the registered test objects
    */
   public List<Class<? extends TestCase>> getRegisteredTests(String testsId, String testsType);

   /**
    * Registers a test class with the service which will be run within the live framework,
    * convenience method for {@link #registerTests(String, String, Class[])}
    * @param testsId a unique id for a set of tests (could be a userId or sessionId or whatever)
    * @param testsType a test type constant (use TESTING_TESTS_XXXX constants) for this testId,
    * NOTE: the type is associated with the testId and not the test class itself, all tests
    * under a single testId are marked as the same type, changing the type will change the type for the testId
    * @param testClass test class which you want to register to be run by this service
    */
   public void registerTest(String testsId, String testsType, Class<? extends TestCase> testClass);

   /**
    * Registers multiple test classes with the service which will be run within a live framework
    * @param testsId a unique id for a set of tests (could be a userId or sessionId or whatever)
    * @param testsType a test type constant (use TESTING_TESTS_XXXX constants) for these tests,
    * NOTE: the type is associated with the testId and not the test class itself, all tests
    * under a single testId are marked as the same type, changing the type will change the type for the testId
    * @param testClasses test classes which you want to register to be run by this service
    */
   public void registerTests(String testsId, String testsType, Class<? extends TestCase>[] testClasses);

   /**
    * Unregister all the tests for a specific test id and/or type,
    * leaving both params null will unregister all tests
    * @param testsId (optional) a unique id for a set of tests (could be a userId or sessionId or whatever),
    * leave this null to match any id
    * @param testsType (optional) a specific type of test (use TESTING_TESTS_XXXX constants), 
    * null will match any type of test
    */
   public void unregisterTests(String testsId, String testsType);

   /**
    * Run the set of tests registered with this test id and/or test type,
    * leaving both params null will run all tests (not recommended)
    * @param testsId (optional) a unique id for a set of tests (could be a userId or sessionId or whatever),
    * leave this null to match any id
    * @param testsType (optional) the type of test (e.g. integration, load, etc.), 
    * this will limit the test ids returned to a specific type 
    * (use TESTING_TESTS_XXXX constants), null will match any type of test
    * @return a map of the registered {@link TestCase} class -> {@link TestResult} for that test case
    */
   public Map<Class<? extends TestCase>, TestResult> runTests(String testsId, String testsType);

   
   /**
    * The number of seconds to delay when automatically running tests on startup,<br/>
    * 0 means run the currently registered tests immediately 
    * (HIGHLY discouraged, may not be all tests in the system),<br/>
    * a positive number will run all the tests registered once the delay has passed,<br/>
    * a negative number will keep the automatic tests from running at all,<br/>
    * default is -1 (no automatic test running)
    * @param seconds the number of seconds to delay
    */
   public void setAutomaticTestDelay(int seconds);

   /**
    * @return the number of seconds to delay test starts when automatically running tests on startup
    * @see #setAutomaticTestDelay(int)
    */
   public int getAutomaticTestDelaySeconds();

   /**
    * Enable this testrunner service to execute tests
    * @param enabled true to enable testing, false otherwise
    */
   public void setTestingEnabled(boolean enabled);

   /**
    * @return true if testing is enabled for this service
    * (i.e. if this service will execute tests), false otherwise
    */
   public boolean isTestingEnabled();

   /**
    * Set one of the test runner parameters programatically,
    * see the constants for details about what each one does
    * @param parameterConstant one of the TESTING_TESTS_XXXXX constants
    * @param value set the parameter to this value
    */
   public void setTestRunnerParam(String parameterConstant, boolean value);

   /**
    * Get one of the test runner parameters programatically,
    * see the constants for details about what each one does<br/>
    * NOTE: to make this easy to use, this will always return false when testing is not active (disabled)
    * @param parameterConstant one of the TESTING_TESTS_XXXXX constants
    * @return the value for this param
    */
   public boolean getTestRunnerParam(String parameterConstant);

}
