/**
 * SakaiTestCase.java - test-runner - 2007 Nov 21, 2007 9:02:05 AM - azeckoski
 */

package org.sakaiproject.testrunner.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PropertyResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.testrunner.utils.annotations.Autowired;
import org.sakaiproject.testrunner.utils.annotations.Resource;
import org.sakaiproject.thread_local.cover.ThreadLocalManager;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolSession;

/**
 * Special testcase for running Sakai tests<br/>
 * Includes all the functionality of the {@link SpringTestCase}, see it for details<br/>
 * <br/>
 * Includes support for setting a user for a test which will be reset back to the 
 * current user at the end of the test
 * Also includes support for emulating the start of a request and end of a request
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class SakaiTestCase extends SpringTestCase {
	private static final Log log = LogFactory.getLog(SakaiTestCase.class);
	protected static Object compMgr;
	
   private SessionManager sessionManager;
   @Resource(name="org.sakaiproject.tool.api.SessionManager")
   public void setSessionManager(SessionManager sessionManager) {
      this.sessionManager = sessionManager;
   }

   private AuthzGroupService authzGroupService;
   @Autowired
   public void setAuthzGroupService(AuthzGroupService authzGroupService) {
      this.authzGroupService = authzGroupService;
   }

   public final String SUPER_USER = "admin";
   protected final String CURRENT_USER_MARKER = "originalTestUser";
   private boolean hijackedSession = false;

   /**
    * Change the current user for a test or test thread to the admin user,
    * this will store the original user in the session and then restore it 
    * at the end so it is safe to call this in setup
    * (though you can call it from anywhere)
    */
   public void setSuperUser() {
      setTestUser(SUPER_USER);
   }

   /**
    * Change the current user for a test or test thread to another user,
    * this will store the original user in the session and then restore it 
    * at the end so it is safe to call this in setup
    * (though you can call it from anywhere), Note that it will only restore the user which
    * was the current user when the method was originally called
    * @param userId the internal Sakai user id (not the username) of the user to switch
    * to as the current user, you can look this up using the UserDirectoryService or
    * create a user just for testing
    */
   public void setTestUser(String userId) {
      Session currentSession = sessionManager.getCurrentSession();
      if (currentSession == null) {
         // start a session if none is around
         if (userId == null) {
            currentSession = sessionManager.startSession();
         } else {
            currentSession = sessionManager.startSession(userId);
         }
      } else {
         hijackedSession = true;
      }
      if (currentSession.getAttribute(CURRENT_USER_MARKER) == null) {
         // only set this if it is not already set
         String currentUserId = currentSession.getUserId();
         if (currentUserId == null) {
            currentUserId = "";
         }
         currentSession.setAttribute(CURRENT_USER_MARKER, currentUserId);
      }
      currentSession.setUserId(userId);
      currentSession.setActive();
      sessionManager.setCurrentSession(currentSession);
      authzGroupService.refreshUser(userId);
   }

   @Override
   protected void tearDown() throws Exception {
      if (emulatingRequest) {
         // handle the request cleanup in case it was not done
         endEmulatedRequest();
      }
      if (hijackedSession) {
         // switch user session back if it was taken over for the test
         Session currentSession = sessionManager.getCurrentSession();
         String currentUserId = null;
         if (currentSession != null) {
            currentUserId = (String) currentSession.getAttribute(CURRENT_USER_MARKER);
            if (currentUserId != null) {
               currentSession.removeAttribute(CURRENT_USER_MARKER);
               if ("".equals(currentUserId)) {
                  // no current user so blow away any that were set during testing
                  currentSession.clear();
                  sessionManager.setCurrentSession(currentSession);
               } else {
                  currentSession.setUserId(currentUserId);
                  sessionManager.setCurrentSession(currentSession);
                  authzGroupService.refreshUser(currentUserId);
               }
            }
         }
      }
      super.tearDown();
   }

   /*
    * This stuff below supports the emulation of a request, there is a lot of duplication
    * because things are not well exposed in the RequestFilter -AZ
    */

   protected final static String CURRENT_REMOTE_USER = "org.sakaiproject.util.RequestFilter.remote_user";
   protected final static String CURRENT_CONTEXT = "org.sakaiproject.util.RequestFilter.context";
   protected final static String CURRENT_SESSION = "org.sakaiproject.api.kernel.session.current";
   protected final static String CURRENT_TOOL_SESSION = "org.sakaiproject.api.kernel.session.current.tool";
   protected final static String CURRENT_HTTP_SESSION = "org.sakaiproject.util.RequestFilter.http_session";

   protected Boolean currentRemoteUser = false;
   protected String currentContext = null;
   protected Session currentSession = null;
   protected ToolSession currentToolSession = null;
   protected Integer currentHttpSession = 0;


   private boolean emulatingRequest = false;

   /**
    * Emulate the start of a Sakai Request, this will be the same as if the code after this is running
    * inside a the Saaki request filter (as if it were wrapped), this pairs with {@link #endEmulatedRequest()}
    * to wrap a block of code
    * @param userId set the current user id (the internal id) for this request, note that setting this to null will ensure there is no current
    * user set for the emulated request and will clear the current user, use the {@link #SUPER_USER} constant to specify
    * that this request should run as a super user
    */
   public void startEmulatedRequest(String userId) {
      // only emulate if we are not already emulating the request
      if (!emulatingRequest) {
         emulatingRequest = true;
         // store the current values from this threads ThreadLocalManager
         currentRemoteUser = (Boolean) ThreadLocalManager.get(CURRENT_REMOTE_USER);
         currentContext = (String) ThreadLocalManager.get(CURRENT_CONTEXT);
         currentSession = (Session) ThreadLocalManager.get(CURRENT_SESSION);
         currentToolSession = (ToolSession) ThreadLocalManager.get(CURRENT_TOOL_SESSION);
         currentHttpSession = (Integer) ThreadLocalManager.get(CURRENT_HTTP_SESSION);

         if (userId != null) {
            setTestUser(userId);
         }

         // Sakai request filter guarantees a session
         Session cs = sessionManager.getCurrentSession();
         if (cs == null) {
            Session s = sessionManager.startSession();
            sessionManager.setCurrentSession(s);
         }
         // Sakai request filter guarantees a tool session with placementId (if there is a placement), we will assume there isn't for now
//         String placementId = "";
//         ToolSession toolSession = toolSession = currentSession.getToolSession(placementId);
//         sessionManager.setCurrentToolSession(toolSession);
      }
   }

   /**
    * Emulate the end of a Sakai Request, this should be run to clear out the request data 
    */
   public void endEmulatedRequest() {
      // only clear if we are emulating a request already
      if (emulatingRequest) {
         emulatingRequest = false;
         // wipe out all stored TL cache data
         ThreadLocalManager.clear();
         // restore the current values
         ThreadLocalManager.set(CURRENT_REMOTE_USER, currentRemoteUser);
         ThreadLocalManager.set(CURRENT_CONTEXT, currentContext);
         ThreadLocalManager.set(CURRENT_SESSION, currentSession);
         ThreadLocalManager.set(CURRENT_TOOL_SESSION, currentToolSession);
         ThreadLocalManager.set(CURRENT_HTTP_SESSION, currentHttpSession);
      }
   }
   
   /*
    * From SakaiTestBase
    */
   
	
	/**
	 * Initialize the component manager once for all tests, and log in as admin.
	 */
	protected static void oneTimeSetup() throws Exception {
		if(compMgr == null) {
			// Find the sakai home dir
			String tomcatHome = getTomcatHome();
			String sakaiHome = getSakaiHome(tomcatHome);
			String componentsDir = tomcatHome + "components/";
			
			// Set the system properties needed by the sakai component manager
			System.setProperty("sakai.home", sakaiHome);
			System.setProperty("sakai.components.root", componentsDir);

			log.debug("Starting the component manager");

			// Add the sakai jars to the current classpath.  Note:  We are limited to using the sun jvm now
			URL[] sakaiUrls = getJarUrls(new String[] {tomcatHome + "common/endorsed/",
					tomcatHome + "common/lib/", tomcatHome + "shared/lib/"});
			URLClassLoader appClassLoader = (URLClassLoader)Thread.currentThread().getContextClassLoader();
			Method addMethod = URLClassLoader.class.getDeclaredMethod("addURL", new Class[] {URL.class});
			addMethod.setAccessible(true);
			for(int i=0; i<sakaiUrls.length; i++) {
				addMethod.invoke(appClassLoader, new Object[] {sakaiUrls[i]});
			}
			
			compMgr = org.sakaiproject.component.cover.ComponentManager.getInstance();
			/*
			 try{
			Class clazz = Class.forName("org.sakaiproject.component.cover.ComponentManager");
			compMgr = clazz.getDeclaredMethod("getInstance", (Class[])null).invoke((Object[])null, (Object[])null);
			}catch (Exception e){
				
				log.error("!!!!!!!! Component manager cannot be instantiated... dependencies will not be injected !!!!!!!");
				
			}*/

			log.debug("Finished starting the component manager");
		}
	}

	/**
	 * Close the component manager when the tests finish.
	 */
	public static void oneTimeTearDown() {
		//SessionManager.getCurrentSession().invalidate();
		if(compMgr != null) {
			try {
				Method closeMethod = compMgr.getClass().getMethod("close", new Class[0]);
				closeMethod.invoke(compMgr, new Object[0]);
			} catch (Exception e) {
				log.error(e);
			}
		}
	}

	/**
	 * Fetches the "maven.tomcat.home" property from the maven build.properties
	 * file located in the user's $HOME directory.
	 * 
	 * @return
	 * @throws Exception
	 */
	private static String getTomcatHome() throws Exception {
		String testTomcatHome = System.getProperty("test.tomcat.home");
		if ( testTomcatHome != null && testTomcatHome.length() > 0 ) {
			log.debug("Using tomcat home: " + testTomcatHome);
			return testTomcatHome;
		} else {
			String homeDir = System.getProperty("user.home");
			File file = new File(homeDir + File.separatorChar + "build.properties");
			FileInputStream fis = new FileInputStream(file);
			PropertyResourceBundle rb = new PropertyResourceBundle(fis);
			String tomcatHome = rb.getString("maven.tomcat.home");
			log.debug("Tomcat home = " + tomcatHome);
			return tomcatHome;
		}
	}
	
	private static String getSakaiHome(String tomcatHome) throws Exception {
		String sakaiHome = System.getProperty("test.sakai.home");
		if (sakaiHome == null) {
			sakaiHome = tomcatHome + File.separatorChar + "sakai" + File.separatorChar;
		}
		return sakaiHome;
	}
	
	/**
	 * Builds an array of file URLs from a directory path.
	 * 
	 * @param dirPath
	 * @return
	 * @throws Exception
	 */
	private static URL[] getJarUrls(String dirPath) throws Exception {
		File dir = new File(dirPath);
		File[] jars = dir.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				if(pathname.getName().startsWith("xml-apis")) {
					return false;
				}
				return true;
			}
		});
		URL[] urls = new URL[jars.length];
		for(int i = 0; i < jars.length; i++) {
			urls[i] = jars[i].toURL();
		}
		return urls;
	}

	private static URL[] getJarUrls(String[] dirPaths) throws Exception {
		List jarList = new ArrayList();
		
		// Add all of the tomcat jars
		for(int i=0; i<dirPaths.length; i++) {
			jarList.addAll(Arrays.asList(getJarUrls(dirPaths[i])));
		}

		URL[] urlArray = new URL[jarList.size()];
		jarList.toArray(urlArray);
		return urlArray;
	}
	

}
