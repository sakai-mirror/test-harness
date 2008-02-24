/**
 * SampleTestSakaiUser.java - Test Runner - 2007 Aug 11, 2007 11:43:29 PM - AZ
 */

package org.sakaiproject.testrunner.impl.tests;

import org.sakaiproject.testrunner.utils.SpringTestCase;
import org.sakaiproject.testrunner.utils.annotations.Autowired;
import org.sakaiproject.testrunner.utils.annotations.Resource;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;


/**
 * This tests the ability of our tests to get to the various Spring beans,
 * this test will fail if there is no UDS available,
 * Uses annotations to define how to get the beans from Spring
 *
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class SampleTestSakaiUser extends SpringTestCase {

   private UserDirectoryService userDirectoryService;
   @Autowired
   public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
      this.userDirectoryService = userDirectoryService;
   }

   private PreferencesService preferencesService;
   @Resource(name="org.sakaiproject.user.api.PreferencesService")
   public void setPreferencesService(PreferencesService preferencesService) {
      this.preferencesService = preferencesService;
   }


   @Override
   protected void setUp() throws Exception {
      super.setUp();
      // this is the old alternative way (without annotations) -AZ
      //userDirectoryService = getSpringBean(UserDirectoryService.class);
   };

   public void testCanGetSakaiUDSBean() {
      assertNotNull(userDirectoryService);
   }

   public void testCanGetSakaiPrefBean() {
      assertNotNull(preferencesService);
   }

   public void testCanUseUDS() {
      assertTrue(userDirectoryService.countUsers() > 1);
      assertTrue(userDirectoryService.getUsers(1, 1).size() == 1);
      User user = null;
      try {
         user = userDirectoryService.getUser(UserDirectoryService.ADMIN_ID);
      } catch (UserNotDefinedException e) {
         fail("Exception: " + e.getMessage());
      }
      assertNotNull(user);
      assertEquals(UserDirectoryService.ADMIN_ID, user.getId());
   }

   public void testCanUsePrefs() {
      if (preferencesService.allowUpdate("admin")) {
         assertTrue(preferencesService.allowUpdate("admin"));
      }
   }

}
