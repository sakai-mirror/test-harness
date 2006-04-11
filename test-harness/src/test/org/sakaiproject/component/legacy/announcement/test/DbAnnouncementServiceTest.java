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

package org.sakaiproject.component.legacy.announcement.test;

import junit.extensions.TestSetup;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.sakaiproject.service.legacy.announcement.AnnouncementChannel;
import org.sakaiproject.service.legacy.announcement.AnnouncementMessageEdit;
import org.sakaiproject.service.legacy.announcement.AnnouncementService;
import org.sakaiproject.service.legacy.entity.EntityManager;
import org.sakaiproject.service.legacy.entity.Reference;
import org.sakaiproject.service.legacy.site.Site;
import org.sakaiproject.service.legacy.site.SiteService;
import org.sakaiproject.test.SakaiTestBase;

public class DbAnnouncementServiceTest extends SakaiTestBase {
	private AnnouncementService announcementService;
	private EntityManager entityManager;
	private SiteService siteService;
	private Site site;
	
	/**
	 * Runs only once for this TestCase, so we can keep the same component manager
	 * rather than rebuilding it for each test.
	 * 
	 * @return
	 */
	public static Test suite() {
		TestSetup setup = new TestSetup(new TestSuite(DbAnnouncementServiceTest.class)) {
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
		announcementService = (AnnouncementService)getService("org.sakaiproject.service.legacy.announcement.AnnouncementService");
		entityManager = (EntityManager)getService("org.sakaiproject.service.legacy.entity.EntityManager");
		siteService = (SiteService)getService("org.sakaiproject.service.legacy.site.SiteService");
		
		// Set username as admin
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
	
	public void testAnnouncementChannel() throws Exception {
		// Add a new channel
		String ref = announcementService.channelReference(site.getId(),  SiteService.MAIN_CONTAINER);
		AnnouncementChannel annChannel = announcementService.addAnnouncementChannel(ref);
		
		// Add a message
		String msgBody = "This is a message to all my students.  Wake up!";
		AnnouncementMessageEdit message = annChannel.addAnnouncementMessage();
		message.setBody(msgBody);
		annChannel.commitMessage(message);
		
		// Get the message's reference
		Reference msgRef = entityManager.newReference(message.getReference());
		
		// Ensure that we can get the message
		Assert.assertTrue(announcementService.getMessage(msgRef) != null);
		Assert.assertEquals(announcementService.getMessage(msgRef).getBody(), msgBody);
	}
}


