/**
 * SampleTestTwo.java - Test Runner - 2007 Aug 11, 2007 11:43:29 PM - AZ
 */

package org.sakaiproject.testrunner.test.samples;

import junit.framework.TestCase;


/**
 * This tests the ability of our test to simply run correctly,
 * this test is designed to have an error
 *
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class SampleTestThree extends TestCase {

   protected String[] array1 = new String[] {"0","1"};

   /* (non-Javadoc)
    * @see junit.framework.TestCase#setUp()
    */
   protected void setUp() throws Exception {
      super.setUp();
   }

   /* (non-Javadoc)
    * @see junit.framework.TestCase#tearDown()
    */
   protected void tearDown() throws Exception {
      super.tearDown();
   }

   public void testCheckArray() {
      // see if this test will work
      assertTrue(array1[0].equals("0"));
   }

   public void testCauseError() {
      // this test should cause an error
      assertTrue(array1[2].equals("2"));
   }

}
