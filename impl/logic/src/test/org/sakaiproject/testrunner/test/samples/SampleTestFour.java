/**
 * SampleTestFour.java - Test Runner - 2007 Aug 11, 2007 11:43:29 PM - AZ
 */

package org.sakaiproject.testrunner.test.samples;

import junit.framework.TestCase;


/**
 * This tests the ability of our test to simply run correctly,
 * simplest possible test
 *
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class SampleTestFour extends TestCase {

   public void testSafeCheck() {
      // shouls always work
      assertTrue(1 == 1);
   }

}
