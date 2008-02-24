/**
 * SampleTestOne.java - Test Runner - 2007 Aug 11, 2007 11:42:57 PM - AZ
 */

package org.sakaiproject.testrunner.test.samples;

import junit.framework.TestCase;


/**
 * Sample test case to see if things are working
 *
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class SampleTestOne extends TestCase {

   private boolean checkSetup = false;

   /* (non-Javadoc)
    * @see junit.framework.TestCase#setUp()
    */
   protected void setUp() throws Exception {
      super.setUp();
      checkSetup = true;
   }

   /* (non-Javadoc)
    * @see junit.framework.TestCase#tearDown()
    */
   protected void tearDown() throws Exception {
      super.tearDown();
   }

   public void testCheckOne() {
      // see if this test will work
      assertTrue(1 == 1);
   }

   public void testCheckTwo() {
      // see if this test will work
      assertFalse(1 == 2);
   }

   public void testCheckSetup() {
      assertTrue(checkSetup);
   }

}
