/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2008 The Regents of the University of California
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
import java.io.FileFilter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Emulate the Sakai component environment set up by a running Tomcat container. 
 */
public class ComponentContainerEmulator {
	private static final Log log = LogFactory.getLog(ComponentContainerEmulator.class);
	private static Object componentManager;
	
	public static void startComponentManager(String tomcatHome, String sakaiHome) throws Exception {
		if (log.isDebugEnabled()) log.debug("Starting the component manager; sakaiHome=" + sakaiHome + ", tomcatHome=" + tomcatHome);

		// Set the system properties needed by the sakai component manager
		System.setProperty("sakai.home", sakaiHome);
		System.setProperty("sakai.components.root", tomcatHome + "components/");
		
		// Add the sakai jars to the current classpath.  Note:  We are limited to using the sun jvm now
		URL[] sakaiUrls = getJarUrls(new String[] {tomcatHome + "common/endorsed/",
				tomcatHome + "common/lib/", tomcatHome + "shared/lib/"});
		URLClassLoader appClassLoader = (URLClassLoader)Thread.currentThread().getContextClassLoader();
		Method addMethod = URLClassLoader.class.getDeclaredMethod("addURL", new Class[] {URL.class});
		addMethod.setAccessible(true);
		for(int i=0; i<sakaiUrls.length; i++) {
			addMethod.invoke(appClassLoader, new Object[] {sakaiUrls[i]});
		}
		
		Class<?> clazz = Class.forName("org.sakaiproject.component.cover.ComponentManager");
		componentManager = clazz.getDeclaredMethod("getInstance", (Class[])null).invoke((Object[])null, (Object[])null);

		if (log.isDebugEnabled()) log.debug("Finished starting the component manager");
	}
	
	public static void stopComponentManager() {
		if(componentManager != null) {
			try {
				Method closeMethod = componentManager.getClass().getMethod("close", new Class[0]);
				closeMethod.invoke(componentManager, new Object[0]);
			} catch (Exception e) {
				log.error(e);
			}
		}
	}
	
	public static boolean isStarted() {
		return (componentManager != null);
	}
	
	/**
	 * Convenience method to get a service bean from the Sakai component manager.
	 * 
	 * @param beanId The id of the service
	 * 
	 * @return The service, or null if the ID is not registered
	 */
	public static final Object getService(String beanId) {
		try {
			Method getMethod = componentManager.getClass().getMethod("get", new Class[] {String.class});
			return getMethod.invoke(componentManager, new Object[] {beanId});
		} catch (Exception e) {
			log.error(e);
			return null;
		}
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
		List<URL> jarList = new ArrayList<URL>();
		
		// Add all of the tomcat jars
		for(int i=0; i<dirPaths.length; i++) {
			jarList.addAll(Arrays.asList(getJarUrls(dirPaths[i])));
		}

		URL[] urlArray = new URL[jarList.size()];
		jarList.toArray(urlArray);
		return urlArray;
	}
}
