/**
 * TestRunnerServiceImpl.java - Test Runner - 2007 Aug 11, 2007 10:55:41 PM - AZ
 */

package org.sakaiproject.testrunner.impl;

import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import junit.framework.TestCase;
import junit.framework.TestResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.testrunner.TestRunnerService;
import org.sakaiproject.testrunner.impl.tests.util.IntegrationTestLogAdaptor;
import org.sakaiproject.testrunner.utils.TestRunnerUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


/**
 * implementation for the test runner service
 *
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class TestRunnerServiceImpl implements TestRunnerService, ApplicationContextAware {

   
   private IntegrationTestLogAdaptor log = null;
   private Log commonsLog = LogFactory.getLog(TestRunnerServiceImpl.class);
   private final static String BUILDTIME_CLASSLOADER_PKG_NAME_FRAGMENT = "org.apache.maven";
   private boolean isMavenRunning = false;

   private ApplicationContext applicationContext;
   public void setApplicationContext(ApplicationContext applicationContext) {
      this.applicationContext = applicationContext;
   }

   protected boolean enabled = true;
   /**
    * @param enabled true to enable this service to run tests, false to disable all test running
    */
   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   protected boolean testIntegration = true;
   /**
    * @param testIntegration true to enable integration tests, false to disable
    */
   public void setTestIntegration(boolean testIntegration) {
      this.testIntegration = testIntegration;
   }

   protected boolean testLoad = false;
   /**
    * @param testLoad true to enable load tests, false to disable
    */
   public void setTestLoad(boolean testLoad) {
      this.testLoad = testLoad;
   }

   protected boolean testValidity = false;
   /**
    * @param testValidity true to enable data validation tests, false to disable
    */
   public void setTestValidity(boolean testValidity) {
      this.testValidity = testValidity;
   }

   /**
    * @see #setTestDelaySeconds(int)
    */
   protected int testDelaySeconds = -1;
   /**
    * The number of seconds to delay when automatically running tests on startup,<br/>
    * 0 means run the currently registered tests immediately 
    * (HIGHLY discouraged, may not be all tests in the system),<br/>
    * a positive number will run all the tests registered once the delay has passed,<br/>
    * a negative number will keep the automatic tests from running at all,<br/>
    * default is -1 (no automatic test running)
    * @param testDelaySeconds the number of seconds to delay
    */
   public void setTestDelaySeconds(int testDelaySeconds) {
      this.testDelaySeconds = testDelaySeconds;
   }


   protected Map<String, Set<Class<? extends TestCase>>> testIdMap = 
      new ConcurrentHashMap<String, Set<Class<? extends TestCase>>>();
   /** 
    * for testing
    * @param testIdMap this is map of testId => Set(TestCase)
    */
   public void setTestIdMap(Map<String, Set<Class<? extends TestCase>>> testIdMap) {
      this.testIdMap = testIdMap;
   }

   protected Map<String, Set<String>> testTypeMap = 
      new ConcurrentHashMap<String, Set<String>>();
   /** 
    * for testing
    * @param testTypeMap this is map of testType => Set(testsId)
    */
   public void setTestTypeMap(Map<String, Set<String>> testTypeMap) {
      this.testTypeMap = testTypeMap;
   }

   public TestRunnerServiceImpl(){
	   /*
	    * This is for *this* project's unit testing... if the init() method gets called
	    * it will override this...
	    * 
	    */ 
	   commonsLog = LogFactory.getLog(TestRunnerServiceImpl.class);
	   log = new IntegrationTestLogAdaptor(this, false); 	// using 'this' in the constructor is 
	   										// unadvisable but I can't see the harm 
	   										// for unit testing nor a better way
   }

   public void init() {
      commonsLog.debug("init: using application context: " + applicationContext.getDisplayName());

      URLClassLoader appClassLoader = (URLClassLoader)Thread.currentThread().getContextClassLoader();
	  String cloaderPkgName = appClassLoader.getClass().getName();
	  
	  setMavenRunning( cloaderPkgName.contains(BUILDTIME_CLASSLOADER_PKG_NAME_FRAGMENT) );
	  
	  log = new IntegrationTestLogAdaptor(this, isMavenRunning());
	  
      log.info("Testrunner is " + (enabled ? "enabled" : "DISABLED") 
    		+ ": running in: " + (isMavenRunning() ? "maven (build-time)" : "an Application Server (run-time)")
            + ": integration testing: " + (testIntegration ? "on" : "OFF")
            + ": load testing: " + (testLoad ? "on" : "OFF")
            + ": data validation testing: " + (testValidity ? "on" : "OFF")
            + ": automatic tests: " + (testDelaySeconds >= 0 ? "on ("+testDelaySeconds+" secs)" : "OFF")
      );

      // code to automatically run all registered tests
      if (testDelaySeconds >= 0) {
         if (isTestingEnabled()) {
            log.info("Automatic TestRunner test runs enabled: Executing all registered tests for enabled test types");
            if (testDelaySeconds > 0) {
               log.debug("Delaying test execution for " + testDelaySeconds + " secs");
               TimerTask runTestTask = new TimerTask() {
                  @Override
                  public void run() {
                     int testCount = getRegisteredTests(null, null).size();
                     log.info("Executing all "+testCount+" TestRunner tests (delayed run)...");
                     List<String> summaries = TestRunnerUtils.summarizeTestsResults( runTests(null, null) );
                     for (String summary : summaries) {
                        log.info(summary);
                     }
                     log.info("All TestRunner tests complete");
                  }};
                  Timer timer = new Timer(true);
                  timer.schedule(runTestTask, testDelaySeconds * 1000);
            } else {
               log.info("Executing all current TestRunner tests (immediate run, this is HIGHLY discouraged)...");
               List<String> summaries = TestRunnerUtils.summarizeTestsResults( runTests(null, null) );
               for (String summary : summaries) {
                  log.info(summary);
               }
               log.info("All TestRunner tests complete");
            }
         } else {
            log.warn("TestRunner testing DISABLED, cannot run all tests");
         }
      }
   }


   public List<Class<? extends TestCase>> getRegisteredTests(String testsId, String testsType) {
      List<Class<? extends TestCase>> l = null;
      Set<Class<? extends TestCase>> s = null;
      if (testsId == null && testsType == null) {
         // return all registered tests
         s = new HashSet<Class<? extends TestCase>>();
         for (Set<Class<? extends TestCase>> list : testIdMap.values()) {
            s.addAll(list);
         }
      } else if (testsId == null) {
         // get by type only
         checkTestsType(testsType); // verify type is valid
         s = new HashSet<Class<? extends TestCase>>();
         List<String> ids = getTestsIds(testsType);
         for (String id : ids) {
            s.addAll(testIdMap.get(id));
         }
      } else if (testsType == null) {
         // get by id only
         s = testIdMap.get(testsId);
      } else {
         // get by id and type
         checkTestsType(testsType); // verify type is valid
         s = testIdMap.get(testsId);
         if (s != null && s.size() > 0) {
            if (testTypeMap.get(testsType) == null || 
                  ! testTypeMap.get(testsType).contains(testsId)) {
               // if this test is not of the type specified then do not return it
               s = null;
            }
         }
      }

      if (s == null) {
         l = new ArrayList<Class<? extends TestCase>>(0);
      } else {
         l = new ArrayList<Class<? extends TestCase>>(s);
      }
      return l;
   }


   public List<String> getTestsIds(String testsType) {
      List<String> l = null;
      Set<String> s = null;
      if (testsType == null) {
         s = testIdMap.keySet();
      } else {
         s = testTypeMap.get(testsType);
      }
      if (s == null) {
         l = new ArrayList<String>(0);
      } else {
         l = new ArrayList<String>(s);
         Collections.sort(l);
      }
      return l;
   }


   public String getTestTypeForTestsId(String testsId) {
      String testType = null;
      if (testIdMap.containsKey(testsId)) {
         for (Iterator<String> iterator = testTypeMap.keySet().iterator(); iterator.hasNext();) {
            String type = iterator.next();
            if (testTypeMap.get(type).contains(testsId)) {
               testType = type;
               break;
            }           
         }
      }
      return testType;
   }

   public List<String> getTestsTypes() {
      return new ArrayList<String>(testTypeMap.keySet());
   }


   public Map<Class<? extends TestCase>, TestResult> runTests(String testsId, String testsType) {
	   
      Map<Class<? extends TestCase>, TestResult> m = null;

      if (enabled) {
         if (testsId == null && testsType == null) {
            // run all tests
            // NOTE: have to be sneaky here and run them in groups by types and also filter out duplicates
            // first we get all the registered tests
            List<Class<? extends TestCase>> all = getRegisteredTests(null, null);
            // second we go through the types of tests and remove the ones that are disabled
            for (String type : testTypeMap.keySet()) {
               if (! checkTestsType(type)) {
                  List<Class<? extends TestCase>> l = getRegisteredTests(null, type);
                  all.removeAll(l);
                  log.warn("TestRunner cannot run tests of type: " + type + " for all tests run, this type is currently DISABLED");
               }
            }
            // finally we run the remaining tests from the all tests run
            if (!all.isEmpty()) {
               m = new HashMap<Class<? extends TestCase>, TestResult>(all.size());
               for (Class<? extends TestCase> testClass : all) {
                  TestResult result = TestRunnerUtils.doSpringTestRun(testClass, applicationContext);
                  m.put(testClass, result);
               }
            } else {
               log.warn("No tests to run for all tests run, if you have tests registered you might want to check that there are some that are enabled");
            }
         } else if (testsId == null) {
            // type is set
            m = executeTests(null, testsType);
         } else if (testsType == null) {
            // id is set
            // get the type associated with this testId
            for (String type : testTypeMap.keySet()) {
               Set<String> s = testTypeMap.get(type);
               if (s.contains(testsId)) {
                  testsType = type;
                  break;
               }
            }
            if (testsType != null) {
               m = executeTests(testsId, testsType);
            }
         } else {
            // both set
            m = executeTests(testsId, testsType);
         }
      } else {
         log.warn("TestRunner is DISABLED on this system: Tests cannot be run");
      }

      if (m == null) {
         m = new HashMap<Class<? extends TestCase>, TestResult>(0);
      }
      
	   
      
	   return m;
   }


   /**
    * Run these tests
    * @param testsId
    * @param testsType
    * @return a map of test results or null if none run
    */
   private Map<Class<? extends TestCase>, TestResult> executeTests(String testsId,
         String testsType) {
      Map<Class<? extends TestCase>, TestResult> m = null;
      if (checkTestsType(testsType)) {
         List<Class<? extends TestCase>> l = getRegisteredTests(testsId, testsType);
         if (l.isEmpty()) {
            log.warn("No tests to run for id: " + testsId + " and type: " + testsType);
         } else {
            m = new HashMap<Class<? extends TestCase>, TestResult>();
            for (Class<? extends TestCase> testClass : l) {
               TestResult result = TestRunnerUtils.doSpringTestRun(testClass, applicationContext);
               m.put(testClass, result);
            }
         }
      } else {
         log.warn(testsType + " test type is DISABLED on this system: Tests cannot be run");            
      }
      return m;
   }


   /**
    * This will check if a testsType is currently active
    * @param testsType
    * @return true if active, false otherwise
    */
   private boolean checkTestsType(String testsType) {
      boolean active = false;
      if (TESTING_TESTS_INTEGRATION.equals(testsType)) {
         active = testIntegration;
      } else if (TESTING_TESTS_LOAD.equals(testsType)) {
         active = testLoad;
      } else if (TESTING_TESTS_VALIDATION.equals(testsType)) {
         active = testValidity;
      } else {
         throw new IllegalArgumentException("Invalid test runner parameter: " + testsType);
      }
      return active;
   }


   @SuppressWarnings("unchecked")
   public void registerTest(String testsId, String testsType, Class<? extends TestCase> testClass) {
      registerTests(testsId, testsType, new Class[] {testClass});
   }


   public void registerTests(String testsId, String testsType, Class<? extends TestCase>[] testCases) {
      if (testsId == null || testsId.length() == 0) {
         throw new IllegalArgumentException("Invalid testsId, cannot be null or zero length");
      }
      if (testsType == null || testsType.length() == 0) {
         throw new IllegalArgumentException("Invalid testsType, cannot be null or zero length");
      }

      // add the tests to the test ids map
      Set<Class<? extends TestCase>> idTests = testIdMap.get(testsId);
      if (idTests == null) {
         idTests = new HashSet<Class<? extends TestCase>>();
      } else {
         // this already exists so we might need to clear the old one
         unregisterTests(testsId, testsType);
      }
      for (int i = 0; i < testCases.length; i++) {
         idTests.add(testCases[i]);
      }
      testIdMap.put(testsId, idTests);

      // add the testId to the test types map
      Set<String> ids = testTypeMap.get(testsType);
      if (ids == null) {
         ids = new HashSet<String>();
      }
      for (int i = 0; i < testCases.length; i++) {
         ids.add(testsId);
      }
      testTypeMap.put(testsType, ids);
   }


   public void unregisterTests(String testsId, String testsType) {
      if (testsId == null && testsType == null) {
         // clear all registered tests
         testIdMap.clear();
         testTypeMap.clear();
      } else if (testsId == null) {
         // clear all of a type
         Set<String> ids = testTypeMap.get(testsType);
         if (ids != null) {
            for (String id : ids) {
               testIdMap.remove(id);
            }
            testTypeMap.remove(testsType);
         }         
      } else if (testsType == null) {
         // clear all with a certain id
         testIdMap.remove(testsId);
         // also clear this id from the types map
         for (Set<String> ids : testTypeMap.values()) {
            ids.remove(testsId);
         }
      } else {
         // both are set
         Set<String> ids = testTypeMap.get(testsType);
         if (ids != null) {
            for (String id : ids) {
               if (testsId.equals(id)) {
                  // if we find the matching testId then we clear it from both places
                  testIdMap.remove(id);
                  ids.remove(id);
                  break;
               }
            }
         }         
      }
   }


   public void setTestRunnerParam(String parameterConstant, boolean value) {
      if (TESTING_TESTS_INTEGRATION.equals(parameterConstant)) {
         testIntegration = value;
      } else if (TESTING_TESTS_LOAD.equals(parameterConstant)) {
         testLoad = value;
      } else if (TESTING_TESTS_VALIDATION.equals(parameterConstant)) {
         testValidity = value;
      } else {
         throw new IllegalArgumentException("Invalid test runner parameter: " + parameterConstant);
      }
   }

   public boolean getTestRunnerParam(String parameterConstant) {
      boolean value = false;
      if (TESTING_TESTS_INTEGRATION.equals(parameterConstant)) {
         if (enabled) value = testIntegration;
      } else if (TESTING_TESTS_LOAD.equals(parameterConstant)) {
         if (enabled) value = testLoad;
      } else if (TESTING_TESTS_VALIDATION.equals(parameterConstant)) {
         if (enabled) value = testValidity;
      } else {
         throw new IllegalArgumentException("Invalid test runner parameter: " + parameterConstant);
      }
      return value;
   }

   public boolean isTestingEnabled() {
      return enabled;
   }

   public void setTestingEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   public int getAutomaticTestDelaySeconds() {
      return testDelaySeconds;
   }

   public void setAutomaticTestDelay(int seconds) {
      testDelaySeconds = seconds;
   }

 /* a string test is conducted against the package name of the current class loader:
  *  if it contains org.apache.maven, then it is assumed that this services is being booted
  *  by a ComponentManager that is in turn being booted by a maven build
  */
   private boolean isMavenRunning() {
	   return isMavenRunning;
   }


   private void setMavenRunning(boolean isMavenRunning) {
	   this.isMavenRunning = isMavenRunning;
   }
   
   
   
}
