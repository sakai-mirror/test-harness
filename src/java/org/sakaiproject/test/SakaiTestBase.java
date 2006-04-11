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

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.PropertyResourceBundle;

import junit.framework.TestCase;

import org.apache.catalina.loader.WebappClassLoader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.kernel.component.ComponentManager;
import org.sakaiproject.api.kernel.session.Session;
import org.sakaiproject.api.kernel.session.cover.SessionManager;

/**
 * An extension of JUnit's TestCase that launches the Sakai component manager.
 * Extend this class to run tests in a simulated Sakai environment.
 * 
 * <p>
 * <strong>NOTE:</strong>
 * Starting the component manager is an expensive operation, since it loads all
 * of the service implementations in the system, including database connection
 * pools, hibernate mappings, etc.  To run a test suite, please collect all tests
 * into a single class rather than running a variety of individual test cases.
 * See {@link org.sakaiproject.test.SakaiIntegrationTest} for an example.
 * </p>
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public abstract class SakaiTestBase extends TestCase {
	private static final Log log = LogFactory.getLog(SakaiTestBase.class);

	protected static ComponentManager compMgr;
	
	/**
	 * Initialize the component manager once for all tests, and log in as admin.
	 */
	protected static void oneTimeSetup() throws Exception {
		if(compMgr == null) {
			// Find the sakai home dir
			String tomcatHome = getTomcatHome();
			String sakaiHome = tomcatHome + File.separatorChar + "sakai" + File.separatorChar;
			String componentsDir = tomcatHome + "components/";
			
			// Set the system properties needed by the sakai component manager
			System.setProperty("sakai.home", sakaiHome);
			System.setProperty(ComponentManager.SAKAI_COMPONENTS_ROOT_SYS_PROP, componentsDir);

			// Get a tomcat classloader
			log.debug("Creating a tomcat classloader for component loading");
			WebappClassLoader wcloader = new WebappClassLoader(Thread.currentThread().getContextClassLoader());
			wcloader.start();

			// Initialize spring component manager
			log.debug("Loading component manager via tomcat's classloader");
			Class clazz = wcloader.loadClass(
					"org.sakaiproject.component.kernel.component.SpringCompMgr");
			Constructor constructor = clazz.getConstructor(new Class[] {ComponentManager.class});
			compMgr = (ComponentManager)constructor.newInstance(new Object[] {null});
			Method initMethod = clazz.getMethod("init", new Class[0]);
			initMethod.invoke(compMgr, new Object[0]);
		}
		
		// Sign in as admin
		if(SessionManager.getCurrentSession() == null) {
			SessionManager.startSession();
			Session session = SessionManager.getCurrentSession();
			session.setUserId("admin");
		}
	}

	/**
	 * Close the component manager when the tests finish.
	 */
	public static void oneTimeTearDown() {
		if(compMgr != null) {
			compMgr.close();
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
		String homeDir = System.getProperty("user.home");
		File file = new File(homeDir + File.separatorChar + "build.properties");
		FileInputStream fis = new FileInputStream(file);
		PropertyResourceBundle rb = new PropertyResourceBundle(fis);
		return rb.getString("maven.tomcat.home");
	}
	
	/**
	 * Convenience method to get a service bean from the Sakai component manager.
	 * 
	 * @param beanId The id of the service
	 * 
	 * @return The service, or null if the ID is not registered
	 */
	public static final Object getService(String beanId) {
		return org.sakaiproject.api.kernel.component.cover.ComponentManager.get(beanId);
	}

	/**
	 * Convenience method to set the current user in sakai.  By default, the user
	 * is admin.
	 * 
	 * @param userUid The user to become
	 */
	public static final void setUser(String userUid) {
		Session session = SessionManager.getCurrentSession();
		session.setUserId(userUid);
	}
	
	/**
	 * Convenience method to create a somewhat unique site id for testing.  Useful
	 * in tests that need to create a site to run tests upon.
	 * 
	 * @return A string suitable for using as a site id.
	 */
	protected String generateSiteId() {
		return "site-" + getClass().getName() + "-" + Math.floor(Math.random()*100000);
	}
	
	/**
	 * Returns a dynamic proxy for a service interface.  Useful for testing with
	 * customized service implementations without needing to write custom stubs.
	 * 
	 * @param clazz The service interface class
	 * @param handler The invocation handler that defines how the dynamic proxy should behave
	 * 
	 * @return The dynamic proxy to use as a collaborator
	 */
	public static final Object getServiceProxy(Class clazz, InvocationHandler handler) {
		return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[] {clazz}, handler);
	}

}
