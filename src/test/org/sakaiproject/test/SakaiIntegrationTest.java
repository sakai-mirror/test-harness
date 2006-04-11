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

package org.sakaiproject.test;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.kernel.function.test.FunctionManagerComponentTest;
import org.sakaiproject.component.kernel.id.test.UuidV4IdComponentTest;
import org.sakaiproject.component.kernel.thread_local.test.ThreadLocalManagerComponentTest;
import org.sakaiproject.component.legacy.announcement.test.DbAnnouncementServiceTest;
import org.sakaiproject.component.legacy.contenthosting.test.ContentHostingTest;

public class SakaiIntegrationTest extends SakaiTestBase {
	private static final Log log = LogFactory.getLog(SakaiIntegrationTest.class);

	public static Test suite() {
		try {
			oneTimeSetup();
		} catch (Exception e) {
			log.error("Unable to setup test suite", e);
		}
		
		TestSuite suite = new TestSuite();

		suite.addTestSuite(UuidV4IdComponentTest.class);
		suite.addTestSuite(ThreadLocalManagerComponentTest.class);
		suite.addTestSuite(FunctionManagerComponentTest.class);
		suite.addTestSuite(DbAnnouncementServiceTest.class);
		suite.addTestSuite(ContentHostingTest.class);
		
		return suite;
	}
	
}
