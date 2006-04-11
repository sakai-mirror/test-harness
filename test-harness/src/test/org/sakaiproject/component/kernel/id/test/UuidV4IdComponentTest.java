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

package org.sakaiproject.component.kernel.id.test;

import junit.extensions.TestSetup;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.kernel.id.IdManager;
import org.sakaiproject.test.SakaiTestBase;

public class UuidV4IdComponentTest extends SakaiTestBase {
	private static final Log log = LogFactory.getLog(UuidV4IdComponentTest.class);

	private IdManager idManager;

	/**
	 * Runs only once for this TestCase, so we can keep the same component manager
	 * rather than rebuilding it for each test.
	 * 
	 * @return
	 */
	public static Test suite() {
		TestSetup setup = new TestSetup(new TestSuite(UuidV4IdComponentTest.class)) {
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
		idManager = (IdManager)getService("org.sakaiproject.api.kernel.id.IdManager");
	}

	/**
	 * Ensure that the IdManager can create IDs, and that two of them are not
	 * the same.
	 * 
	 * @throws Exception
	 */
	public void testCreateUuid() throws Exception {
		
		// Create an ID
		String id1 = idManager.createUuid();

		// Create another ID
		String id2 = idManager.createUuid();

		// Ensure that the IDs were created
		Assert.assertTrue(id1 != null);
		Assert.assertTrue(id2 != null);
		
		// Ensure that the IDs are unique
		Assert.assertTrue(!id1.equals(id2));
	}
}
