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
import java.net.URL;
import java.net.URLClassLoader;
import java.util.PropertyResourceBundle;

import junit.framework.TestCase;

import org.apache.catalina.loader.WebappClassLoader;
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

			// Create tomcat-esque classloaders
			log.debug("Creating a tomcat classloaders for component loading");
			URLClassLoader  endorsedLoader = URLClassLoader.newInstance(getFileUrls(tomcatHome + "/common/endorsed/"), Thread.currentThread().getContextClassLoader());
			URLClassLoader commonLoader = URLClassLoader.newInstance(getFileUrls(tomcatHome + "/common/lib/"), endorsedLoader);
			URLClassLoader sharedLoader = URLClassLoader.newInstance(getFileUrls(tomcatHome + "/shared/lib/"), commonLoader);
			final WebappClassLoader componentLoader = new WebappClassLoader(sharedLoader);
			componentLoader.start();

			// Initialize spring component manager
			log.debug("Loading component manager via tomcat's classloader");
			final Class clazz = componentLoader.loadClass("org.sakaiproject.component.impl.SpringCompMgr", true);
			Class compMgrClass = componentLoader.loadClass("org.sakaiproject.component.api.ComponentManager", true);
			Constructor constructor = clazz.getConstructor(new Class[] {compMgrClass});
			compMgr = constructor.newInstance(new Object[] {null});
			log.debug("instantiating new thread for component init");
			MyThread thread = new MyThread(componentLoader, clazz);
			thread.run();
		}
	}

protected static class MyThread extends Thread {
	private Class clazz = null;
	private ClassLoader cl = null;
	public MyThread(ClassLoader cl, Class clazz) {
		super();
		this.clazz = clazz;
		this.cl = cl;
		this.setContextClassLoader(cl);
	}
	
	public void run() {
		Thread innerThread = new Thread(new Runnable() {
			public void run() {
				Method initMethod;
				try {
					log.debug("________________init in " + Thread.currentThread().getContextClassLoader());
					initMethod = clazz.getMethod("init", new Class[0]);
					initMethod.invoke(compMgr, new Object[0]);
				} catch (Exception e) {
					e.printStackTrace();
				}
			};
		});
		innerThread.run();
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
		try {
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
