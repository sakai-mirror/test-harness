/**
 * SampleTestTwo.java - Test Runner - 2007 Aug 11, 2007 11:43:29 PM - AZ
 */

package org.sakaiproject.testrunner.test.samples;

import junit.framework.TestCase;


/**
 * This tests the ability of our test to simply run correctly,
 * this test is designed to have a failure
 *
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class SampleTestTwo extends TestCase {

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

   public void testSafeCheck() {
      // see if this test will work
      assertTrue(1 == 1);
   }

   public void testCauseFailure() {
      // see if this test will work
      assertFalse(1 == 1);
   }

}
