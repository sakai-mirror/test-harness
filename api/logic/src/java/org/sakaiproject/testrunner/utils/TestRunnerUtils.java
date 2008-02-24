/**
 * TestRunnerUtils.java - Test Runner - 2007 Aug 12, 2007 8:14:10 AM - AZ
 */

package org.sakaiproject.testrunner.utils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestFailure;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.testrunner.utils.annotations.Autowired;
import org.sakaiproject.testrunner.utils.annotations.Resource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


/**
 * This provides some utils which will make it easier to deal with the results
 * of tests and allow for nicely formatted test results
 *
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class TestRunnerUtils {

   private final static Log log = LogFactory.getLog(TestRunnerUtils.class);

   /**
    * Checks all test results and summarize the results in a single boolean
    * @param testRunnerResults map of test cases to test results
    * @return true if all tests pass or false if any fail,
    * also will return false if errors occured or there were 
    * no tests to run in one or more test classes or there are no tests in the map
    */
   public static boolean checkTestsSuccess(Map<Class<? extends TestCase>, TestResult> testRunnerResults) {
      boolean success = false;
      if (testRunnerResults != null) {
         for (Class<? extends TestCase> testClass : testRunnerResults.keySet()) {
            TestResult result = testRunnerResults.get(testClass);
            success = result.wasSuccessful();
            if (! success && result.runCount() > 0) {
               break;
            }
         }
      }
      return success;
   }

   /**
    * Generate a simple summary of the test results
    * @param testRunnerResults map of test cases to test results
    * @return a list of strings which indicate the basic results of all each testcase
    */
   public static List<String> summarizeTestsResults(Map<Class<? extends TestCase>, TestResult> testRunnerResults) {
      List<String> l = new ArrayList<String>();
      if (testRunnerResults != null) {
         for (Class<? extends TestCase> testClass : testRunnerResults.keySet()) {
            TestResult result = testRunnerResults.get(testClass);
            l.add( testClass.getSimpleName() + " : " + makeSummary(result) );
         }
      }
      return l;
   }

   /**
    * Get the detailed results of this test result
    * @param result a test result
    * @return a list of strings with the summary as the first item and then a string
    * summarizing each occurring error or failure (if any occurred)
    */
   @SuppressWarnings("unchecked")
   public static List<String> detailedTestResult(TestResult result) {
      List<String> l = new ArrayList<String>();
      l.add( makeSummary(result) );
      if (! result.wasSuccessful()) {
         for (Enumeration<TestFailure> e = result.errors(); e.hasMoreElements(); ) {
            TestFailure failure = e.nextElement();
            Throwable error = failure.thrownException();
            l.add( " Test Error: " + failure.failedTest() + ": " + error.toString() 
                  + ": " + error.getCause() + ": Error trace: " + failure.trace() );
         }
         for (Enumeration<TestFailure> e = result.failures(); e.hasMoreElements(); ) {
            TestFailure failure = e.nextElement();
            Throwable error = failure.thrownException();
            // TODO - parse out the failure trace and get the line number and method failure
            l.add( " Test Failure: " + failure.failedTest() + ": " + failure.exceptionMessage() 
                  + ": " + error.toString()  + ": Failure trace: " + failure.trace() );
         }
      }
      return l;
   }

   /**
    * Create a readable summary string from a test result
    * @param result
    * @return a human readable string
    */
   public static String makeSummary(TestResult result) {
      String summary;
      if (result.wasSuccessful()) {
         // all passed
         summary = "Tests passed : All " + result.runCount() + " test(s) passed";
      } else {
         // some failed
         summary = "Tests failed : Out of " + result.runCount() + " test(s): " + result.errorCount() + 
         " errors and " + result.failureCount() + " failures";
      }
      return summary;
   }

   /**
    * Special method which allows the execution of a test class without registration,
    * this runs a test class and will throw a descriptive exception if any failures
    * or errors occur during the test run, if all tests pass then a log message is
    * produced, can be used to easily run tests via spring<br/>
    * NOTE: this will NOT register this class or keep anything in memory after the tests run
    * @param testClass a test class (extends {@link TestCase}
    * @return the success message if everything runs correctly, no message is returned on
    * failures or errors because exceptions are thrown
    */
   public static String executeSpringTests(Class<? extends TestCase> testClass, ApplicationContext applicationContext) {
      TestResult result = doSpringTestRun(testClass, applicationContext);

      String resultString = null;
      if (result.wasSuccessful()) {
         resultString = TestRunnerUtils.makeSummary(result);
         log.info("TESTING " + testClass.getSimpleName() + ": " + resultString);
      } else {
         List<String> results = TestRunnerUtils.detailedTestResult(result);
         StringBuilder details = new StringBuilder();
         for (String r : results) {
            details.append(r + "\n");
         }
         log.warn("TESTING " + testClass.getSimpleName() + ": " + details.toString());
         // TODO Make this configurable or remove it
         throw new RuntimeException("Test failed for "+testClass+" (details in log): " + results.get(0));
      }
      return resultString;
   }

   public static TestResult doTestRun(Class<? extends TestCase> testClass) {
      return doSpringTestRun(testClass, null);
   }

   /**
    * Execute a test class and return the results
    * @param testClass
    * @return the {@link TestResult} object
    */
   public static TestResult doSpringTestRun(Class<? extends TestCase> testClass, ApplicationContext applicationContext) {
      TestSuite suite = makeSuite(testClass, applicationContext);
      TestResult result = new TestResult();
      suite.run(result);
      return result;
   }

   @SuppressWarnings("unchecked")
   private static TestSuite makeSuite(Class<? extends TestCase> testClass, ApplicationContext applicationContext) {
      TestSuite suite = new TestSuite();

      Class<?> superClass = testClass;
      List<String> testMethods = new ArrayList<String>();
      List<Method> setterMethods = new ArrayList<Method>();
//      Method initMethod = null;
      while (Test.class.isAssignableFrom(superClass)) {
         Method[] methods = superClass.getDeclaredMethods();
         for (int i = 0; i < methods.length; i++) {
            getTestMethod(methods[i], testMethods);
            getSetterMethod(methods[i], setterMethods);
//            if (initMethod == null && isInitMethod(methods[i])) {
//               initMethod = methods[i];
//            }
         }
         superClass = superClass.getSuperclass();
      }

      Map<Method, Object> springBeans = new HashMap<Method, Object>();
      // load up some fun springy stuff if this is the right kind of class
      if (isSpringTest(testClass) && 
            applicationContext != null) {
         for (Method setter : setterMethods) {
            Class<?>[] types = setter.getParameterTypes();
            if (types.length > 0) {
               Class<?> setClass = types[0];
               if (setClass.isAssignableFrom(ApplicationContext.class)) {
                  springBeans.put(setter, applicationContext);
               } else if (setter.isAnnotationPresent(Resource.class)) {
                  Resource r = setter.getAnnotation(Resource.class);
                  String beanId = r.name();
                  if (beanId != null || "".equals(beanId)) {
                     Object bean;
                     try {
                        bean = applicationContext.getBean(beanId);
                     } catch (BeansException e) {
                        log.warn("Invalid resource annotation for setter method ("
                              + setter.getName()+") in test class ("+testClass.getName()
                              + "), no bean can be found with id=" + beanId, e);
                        continue;
                     }
                     if (setClass.isAssignableFrom(bean.getClass())) {
                        springBeans.put(setter, bean);
                     } else {
                        log.warn("Invalid resource annotation for setter method ("
                              + setter.getName()+") in test class ("+testClass.getName()
                              + "), the set type ("+setClass.getName()
                              + ") does not match the bean type");
                     }
                  }
               } else if(setter.isAnnotationPresent(Autowired.class)) {
                  Object bean = null;
                  try {
                     bean = BeanFactoryUtils.beanOfTypeIncludingAncestors(applicationContext, setClass);
                  } catch (NoSuchBeanDefinitionException e1) {
                     log.warn("Invalid autowire annotation for setter method ("
                           + setter.getName()+") in test class ("+testClass.getName()
                           + "), zero or more than one bean found with class=" + setClass.getName(), e1);
                     continue;
                  } catch (BeansException e1) {
                     log.warn("Invalid autowire annotation for setter method ("
                           + setter.getName()+") in test class ("+testClass.getName()
                           + "), no bean can be found with class=" + setClass.getName(), e1);
                     continue;
                  }
                  if (bean == null) {
                     log.warn("Invalid autowire annotation for setter method ("
                           + setter.getName()+") in test class ("+testClass.getName()
                           + "), no bean can be found with class=" + setClass.getName());
                  } else {
                     springBeans.put(setter, bean);
                  }
               }
            }
         }
      }

      for (String method : testMethods) {
         TestCase testInst;
         try {
            testInst = testClass.newInstance();
            testInst.setName(method); // set the method to run for this test instance
         } catch (Exception e) {
            throw new RuntimeException("Failure attempting to create test object for " + testClass, e);
         }
         // loop through and set the beans on the newly created testInst
         for (Method setter : springBeans.keySet()) {
            try {
               setter.invoke(testInst, new Object[] {springBeans.get(setter)});
            } catch (Exception e) {
               log.warn("Invokation failed for setter method ("+setter.getName()
                     +") in test class ("+testClass.getName()+")");
            }
         }
         // run the init method if there is one
//         if (initMethod != null) {
//            try {
//               initMethod.invoke(testInst, new Object[] {});
//            } catch (Exception e) {
//               log.warn("Invokation failed for INIT method ("+initMethod.getName()
//                     +") in test class ("+testClass.getName()+")");
//            }            
//         }
         suite.addTest( testInst );
      }
      return suite;
   }

   /**
    * @param testClass
    * @return true if this is a Spring test, false otherwise
    */
   private static boolean isSpringTest(Class<? extends TestCase> testClass) {
      boolean isSpringTest = false;
      if (SpringTestCase.class.isAssignableFrom(testClass)) {
         isSpringTest = true;
      } else if (ApplicationContextAware.class.isAssignableFrom(testClass)) {
         isSpringTest = true;
      }
      return isSpringTest;
   }

   private static void getTestMethod(Method m, List<String> names) {
      String name = m.getName();
      if (names.contains(name)) return;
      if (isPublicTestMethod(m)) {
         names.add(name);
      }
   }

   private static boolean isPublicTestMethod(Method m) {
      return isTestMethod(m) && Modifier.isPublic(m.getModifiers());
   }

   private static boolean isTestMethod(Method m) {
      String name = m.getName();
      Class<?>[] parameters = m.getParameterTypes();
      Class<?> returnType = m.getReturnType();
      return parameters.length == 0 && name.startsWith("test") && returnType.equals(Void.TYPE);
   }

   private static void getSetterMethod(Method m, List<Method> methods) {
      if (methods.contains(m)) return;
      if (isPublicSetterMethod(m)) {
         methods.add(m);
      }
   }

   private static boolean isPublicSetterMethod(Method m) {
      return isSetMethod(m) && Modifier.isPublic(m.getModifiers());
   }

   private static boolean isSetMethod(Method m) {
      String name = m.getName();
      Class<?>[] parameters = m.getParameterTypes();
      Class<?> returnType = m.getReturnType();
      return parameters.length == 1 && name.startsWith("set") && returnType.equals(Void.TYPE);
   }

//   private static boolean isInitMethod(Method m) {
//      String name = m.getName();
//      Class<?>[] parameters = m.getParameterTypes();
//      Class<?> returnType = m.getReturnType();
//      if (parameters.length == 0 && 
//            name.equals("init") && 
//            returnType.equals(Void.TYPE) && 
//            Modifier.isPublic(m.getModifiers())) {
//         return true;
//      }
//      return false;
//   }

}
