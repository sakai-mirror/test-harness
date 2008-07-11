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

import static org.sakaiproject.test.ComponentContainerEmulator.getPassthroughSystemProperty;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.PropertyResourceBundle;

import junit.framework.TestCase;

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
	
	/**
	 * Initialize the component manager once for all tests.
	 */
	public static void oneTimeSetup() throws Exception {
		if(!ComponentContainerEmulator.isStarted()) {
			String tomcatHome = getTomcatHome();
			String sakaiHome = getPassthroughSystemProperty("test.sakai.home");	// Can be null
			ComponentContainerEmulator.startComponentManager(tomcatHome, sakaiHome);
		}
	}

	/**
	 * Close the component manager when the tests finish.
	 */
	public static void oneTimeTearDown() {
		//SessionManager.getCurrentSession().invalidate();
		if(ComponentContainerEmulator.isStarted()) {
			ComponentContainerEmulator.stopComponentManager();
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
		String testTomcatHome = getPassthroughSystemProperty("test.tomcat.home");
		if ( testTomcatHome != null && testTomcatHome.length() > 0 ) {
			log.debug("Using tomcat home: " + testTomcatHome);
			return testTomcatHome;
		} else {
			String homeDir = getPassthroughSystemProperty("user.home");
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
		return ComponentContainerEmulator.getService(beanId);
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
	public static final <T> T getServiceProxy(Class<T> clazz, InvocationHandler handler) {
		return (T)Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[] {clazz}, handler);
	}
}
