/**
 * TestExecutor.java - Test Runner - 2007 Aug 12, 2007 8:44:05 PM - AZ
 */

package org.sakaiproject.testrunner.util;

import java.util.Timer;
import java.util.TimerTask;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.testrunner.TestRunnerService;
import org.sakaiproject.testrunner.utils.SpringTestCase;
import org.sakaiproject.testrunner.utils.TestRunnerUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


/**
 * This class exists to make it easy to register and trigger running tests 
 * using spring and helps to avoid the classloader issues that would normally be
 * encountered if this kind of thing were attempted. This also allows you to register
 * your tests with the {@link TestRunnerService} without writing any extra code.<br/>
 * <br/>
 * Using this class is easy and only requires a few steps in your code.<br/>
 * 1) Create your test class that extends {@link TestCase} (you can optionally extend {@link SpringTestCase}
 * which will make working with Spring even easier)<br/>
 * 2) Create a spring bean definition for your test case (probably in components.xml in Sakai) like so:
 * <xmp><bean class="org.sakaiproject.testrunner.util.TestExecutor">
      <property name="testClassname" value="org.sakaiproject.testrunner.impl.tests.SimpleTest" />
      <property name="testType" value="testrunner.integration" />
   </bean></xmp><br/>
 * <b>NOTE:</b> the testType values are the strings in the {@link TestRunnerService} named like TESTING_TESTS_XXXXX 
 * 3) Add the maven dependency to your project.xml (in your Sakai pack project) like so:
 * <xmp><dependency>
         <groupId>org.sakaiproject</groupId>
         <artifactId>sakai-testexecutor</artifactId>
         <version>1.0</version>
         <properties>
            <war.bundle>true</war.bundle>
         </properties>
      </dependency></xmp><br/>
 * 4) Add the maven dependencies to the project.xml for the project that contains your tests like so:
 * <xmp><dependency>
         <groupId>junit</groupId>
         <artifactId>junit</artifactId>
         <version>3.8.1</version>
      </dependency>

      <dependency>
         <groupId>sakaiproject</groupId>
         <artifactId>sakai-testrunner-logic-api</artifactId>
         <version>${sakai.version}</version>
      </dependency></xmp><br/>
 * 
 * You should be able to startup tomcat and when the spring beans are loaded the
 * test runner will execute your tests (unless you optionally choose to have the test registered
 * to be manually run later). Failures will stop the spring context from loading
 * (as they should). You will be able to write tests inside the Sakai framework which are 
 * reliable integration tests. You will also be able to depend on your running Sakai system
 * because you will know the tests have passed in the real environment.
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class TestExecutor implements ApplicationContextAware, InitializingBean, DisposableBean {

   private final static Log log = LogFactory.getLog(TestExecutor.class);

   /**
    * @see #setTestClassname(String)
    */
   private String testClassname;
   /**
    * Set the fully qualified classname of a test class to execute the tests for,
    * (e.g. org.sakaiproject.testrunner.impl.tests.SimpleTest)<br/>
    * this class should extend {@link SpringTestCase} (recommended) or {@link TestCase}
    * @param testClassname the fully qualified classname of a test class
    */
   public void setTestClassname(String testClassname) {
      this.testClassname = testClassname;
   }

   /**
    * @see #setTestType(String)
    */
   private String testType = TestRunnerService.TESTING_TESTS_LOAD;
   /**
    * Set the type of test that this is, defaults to least likely to run
    * (which is the load test): {@link TestRunnerService#TESTING_TESTS_LOAD}
    * @param testType a test type constant from {@link TestRunnerService} 
    * (e.g. {@link TestRunnerService#TESTING_TESTS_INTEGRATION})
    */
   public void setTestType(String testType) {
      this.testType = testType;
   }

   /**
    * @see #setTestId(String)
    */
   private String testId;
   /**
    * This is a unique string id for the test, if this is null then one will be generated
    * @param testId a unique string id
    */
   public void setTestId(String testId) {
      this.testId = testId;
   }

   /**
    * @see #setRegisterTest(boolean)
    */
   private boolean registerTest = true;
   /**
    * Register this test with the {@link TestRunnerService},
    * default is true (registered)<br/>
    * <b>NOTE:</b> you may not want to run your test if you are registering it
    * as you might end up running the tests twice
    * @param registerTest true if this test is registered, false otherwise
    */
   public void setRegisterTest(boolean registerTest) {
      this.registerTest = registerTest;
   }

   /**
    * @see #setTestDelaySeconds(int)
    */
   protected int testDelaySeconds = -1;
   /** 
    * The number of seconds to delay when automatically running tests on startup,<br/>
    * 0 means run these tests immediately (may not be all tests in the system),<br/>
    * a positive will run these tests once the delay has passed,<br/>
    * a negative value will keep these from running automatically at all 
    * (they can still be manually executed via the test-runner tool) ,<br/>
    * default is -1 (don't run automatically)<br/>
    * <b>NOTE:</b> These tests will run in their own parallel threads if the value is greater than 0,
    * be careful not to set all tests to the same positive value, leaving this at 0 will run the test
    * in the current startup thread
    * @param testDelaySeconds seconds before starting the test
    */
   public void setTestDelaySeconds(int testDelaySeconds) {
      this.testDelaySeconds = testDelaySeconds;
   }


   public ApplicationContext applicationContext;
   /* (non-Javadoc)
    * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
    */
   public void setApplicationContext(ApplicationContext applicationContext) {
      this.applicationContext = applicationContext;
   }

   protected TestRunnerService testRunnerService;

   /**
    * Run the tests defined in the class with the testClassname
    * (after we have the spring applicationContext and other params)
    * 
    * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
    */
   public void afterPropertiesSet() throws Exception {
      log.debug("Loading TestRunner spring loaded test");
      if (testClassname == null) {
         throw new RuntimeException("You must set the testClassname to the fully qualified classname of a test class before running executeTests");
      }

      Class<?> c;
      try {
         c = this.getClass().getClassLoader().loadClass(testClassname);
      } catch (ClassNotFoundException e) {
         throw new IllegalArgumentException("Cannot find test class with this fully qualified name:" + testClassname, e);
      }

      if (! TestCase.class.isAssignableFrom(c)) {
         throw new IllegalArgumentException("Invalid class type ("+c+"), does not extend TestCase");
      }
      final Class<? extends TestCase> tc = c.asSubclass(TestCase.class);

      testRunnerService = getTestRunnerService();

      if (registerTest) {
         if (testId == null || "".equals(testId)) {
            testId = testClassname + ":" + System.currentTimeMillis();
         }
         testRunnerService.registerTest(testId, testType, tc);
      } else if (testDelaySeconds < 0) {
         log.warn("Invalid test execution configuration: this test ("+testClassname+") will never " +
               "be executed because it is set to not register with the " +
               "TestRunnerService and the delay is < 0 ("+testDelaySeconds+")");
      }

      if (testDelaySeconds >= 0 && 
            testRunnerService.isTestingEnabled()) {
         if (testRunnerService.getTestRunnerParam(testType)) {
            log.debug("Executing TestRunner spring loaded test");
            if (testDelaySeconds > 0) {
               TimerTask runTestTask = new TimerTask() {
                  @Override
                  public void run() {
                     log.info("Executing delayed tests for " + tc.getName());
                     executeTest(tc);
                  }};
                  Timer timer = new Timer(true);
                  timer.schedule(runTestTask, testDelaySeconds * 1000);
            } else {
               log.info("Executing immediate tests for " + tc.getName());
               executeTest(tc);
            }
         } else {
            log.warn("TestRunner testing DISABLED for type:" + testType + " , cannot run spring loaded testrunner test for class: " + testClassname);
         }
      } else {
         log.warn("TestRunner testing DISABLED, cannot run spring loaded testrunner test for class: " + testClassname);
      }
   }

   /**
    * Unregister the tests if they were registered
    * 
    * @see org.springframework.beans.factory.DisposableBean#destroy()
    */
   public void destroy() throws Exception {
      // unregister these tests if they were registered
      if (registerTest) {
         try {
            testRunnerService.unregisterTests(testId, testType);
         } catch (Exception e) {
            // don't let a failure here blow up the shutdown -AZ
            log.warn("Failed to unregister test: " + testId, e);
         }
      }
   }


   /**
    * @return the test runner service if it can be found or die trying
    */
   private TestRunnerService getTestRunnerService() {
      TestRunnerService testRunnerService;
      try {
         testRunnerService = (TestRunnerService) applicationContext.getBean(TestRunnerService.class.getName());
      } catch (BeansException e) {
         throw new IllegalStateException("Cannot get to the TestRunnerService, unable to run springloaded test: " + testClassname);
      }
      return testRunnerService;
   }

   /**
    * Run the test using the given test class
    * @param c
    */
   private void executeTest(Class<? extends TestCase> c) {
      TestRunnerUtils.executeSpringTests(c, applicationContext);
   }

}
