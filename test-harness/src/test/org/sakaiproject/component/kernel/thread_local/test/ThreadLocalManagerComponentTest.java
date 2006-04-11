/**********************************************************************************
 *
 * $Id$
 *
 ***********************************************************************************
 *
 * Copyright (c) 2005 The Regents of the University of California
 * 
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 * 
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 **********************************************************************************/

package org.sakaiproject.component.kernel.thread_local.test;

import java.util.Date;

import junit.extensions.TestSetup;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.sakaiproject.api.kernel.thread_local.ThreadLocalManager;
import org.sakaiproject.test.SakaiTestBase;

public class ThreadLocalManagerComponentTest extends SakaiTestBase {
	ThreadLocalManager threadLocalManager;

	/**
	 * Runs only once for this TestCase, so we can keep the same component manager
	 * rather than rebuilding it for each test.
	 * 
	 * @return
	 */
	public static Test suite() {
		TestSetup setup = new TestSetup(new TestSuite(ThreadLocalManagerComponentTest.class)) {
			protected void setUp() throws Exception {
				oneTimeSetup();
			}
		};
		return setup;
	}

	/**
	 * Setup test fixture (runs once for each test method called)
	 */
	public void setUp() throws Exception {
		threadLocalManager = (ThreadLocalManager)getService("org.sakaiproject.api.kernel.thread_local.ThreadLocalManager");
	}
	
	public void testThreadLocalManager() throws Exception {
		// Create an object to store in the thread local
		Date date = new Date();
		
		// Store the object
		threadLocalManager.set("theDate", date);
		
		// Ensure that we can get the same object out of the thread local
		Assert.assertTrue(threadLocalManager.get("theDate").equals(date));
	}
}


