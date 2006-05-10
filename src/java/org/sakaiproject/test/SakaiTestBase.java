/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Regents of the University of California
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.test;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.PropertyResourceBundle;

import junit.framework.TestCase;

import org.apache.catalina.loader.StandardClassLoader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

	protected static Object compMgr;
	protected static StandardClassLoader sharedLoader;
	
	private static URL[] getFileUrls(String dirPath) throws Exception {
		File dir = new File(dirPath);
		File[] jars = dir.listFiles();
		URL[] urls = new URL[jars.length];
		for(int i = 0; i < jars.length; i++) {
			urls[i] = jars[i].toURL();
			log.debug(urls[i]);
		}
		return urls;
	}
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
			System.setProperty("sakai.components.root", componentsDir);

			// Keep track of the current (integration testing) classloader
			ClassLoader testClassLoader = Thread.currentThread().getContextClassLoader();
			
			// Create tomcat-esque classloaders
			log.debug("Creating tomcat classloaders for component loading");
			StandardClassLoader  endorsedLoader = new StandardClassLoader(getFileUrls(tomcatHome + "/common/endorsed/"), testClassLoader);
			StandardClassLoader commonLoader = new StandardClassLoader(getFileUrls(tomcatHome + "/common/lib/"), endorsedLoader);
			sharedLoader = new StandardClassLoader(getFileUrls(tomcatHome + "/shared/lib/"), commonLoader);
			
			// Initialize spring component manager using the shared classloader
			Thread.currentThread().setContextClassLoader(sharedLoader);
			final Class clazz = sharedLoader.loadClass("org.sakaiproject.component.cover.ComponentManager");
			compMgr = clazz.getDeclaredMethod("getInstance", null).invoke(null, null);
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
	protected static String getTomcatHome() throws Exception {
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
	
	/**
	 * Convenience method to get a service bean from the Sakai component manager.
	 * 
	 * @param beanId The id of the service
	 * 
	 * @return The service, or null if the ID is not registered
	 */
	protected static final Object getService(String beanId) {
		Thread.currentThread().setContextClassLoader(sharedLoader);
		try {
			log.debug("Resolving " + beanId + " using " + compMgr);
			Method getMethod = compMgr.getClass().getMethod("get", new Class[] {String.class});
			return getMethod.invoke(compMgr, new Object[] {beanId});
		} catch (Exception e) {
			log.error(e);
			return null;
		}
	}

	/**
	 * Convenience method to set the current user in sakai.
	 * 
	 * @param userUid The user to become
	 */
	protected final void setUser(String userUid) {
//		Session session = SessionManager.getCurrentSession();
//		session.setUserId("admin");
//		session.setUserEid("admin");
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
