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

package org.sakaiproject.component.legacy.contenthosting.test;

import junit.extensions.TestSetup;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.sakaiproject.service.legacy.content.ContentCollection;
import org.sakaiproject.service.legacy.content.ContentCollectionEdit;
import org.sakaiproject.service.legacy.content.ContentHostingService;
import org.sakaiproject.service.legacy.site.Site;
import org.sakaiproject.service.legacy.site.SiteService;
import org.sakaiproject.test.SakaiTestBase;
import org.sakaiproject.test.SakaiTestTimer;

public class ContentHostingTest extends SakaiTestBase {
	private ContentHostingService contentService;
	private SiteService siteService;
	private Site site;
	
	/**
	 * Runs only once for this TestCase, so we can keep the same component manager
	 * rather than rebuilding it for each test.
	 * 
	 * @return
	 */
	public static Test suite() {
		TestSetup setup = new TestSetup(new TestSuite(ContentHostingTest.class)) {
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
		// Get the services we need for the tests
		contentService = (ContentHostingService)getService("org.sakaiproject.service.legacy.content.ContentHostingService");
		siteService = (SiteService)getService("org.sakaiproject.service.legacy.site.SiteService");

		// Login as admin
		setUser("admin");

		// Create a site to work from
		site = siteService.addSite(generateSiteId(), "course");
	}


	/**
	 * Remove the newly created objects, so we can run more tests with a clean slate.
	 */
	public void tearDown() throws Exception {
		siteService.removeSite(site);
	}
	
	public void testContentService() throws Exception {
		// Make sure we have a reference to the content hosting service
		Assert.assertNotNull(contentService);
		
		// Add a content collection for the site
		SakaiTestTimer timer = new SakaiTestTimer("create resource collection");
		
		String collectionId = contentService.getSiteCollection(site.getId());
		ContentCollectionEdit edit = contentService.addCollection(collectionId);
		contentService.commitCollection(edit);

		timer.logTimeElapsed();

		// Ensure that there aren't any members in the collection
		ContentCollection collection = contentService.getCollection(collectionId);
		Assert.assertEquals(collection.getMembers().size(), 0);
	}
}


