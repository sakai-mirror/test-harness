/**
 * SakaiSpringTestCase.java - Test Runner - 2007 Aug 12, 2007 7:41:04 PM - AZ
 */

package org.sakaiproject.testrunner.utils;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.testrunner.utils.annotations.Autowired;
import org.sakaiproject.testrunner.utils.annotations.Resource;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


/**
 * This is a convenience class to make it easy to have the spring context accessible
 * when running a test case inside a framework, provides the applicationContext
 * and has a few methods to make it easy to get beans<br/>
 * Annotation based injection of beans in the current Spring 
 * {@link ApplicationContext} (available as {@link #applicationContext})
 * is supported via 2 methods:<br/>
 * 1) @Autowired<br/>
 * Add the {@link Autowired} annotation to any public setter and spring will attempt
 * to find a bean of the class type used in the setter and inject it, this
 * will only work if there are no other beans in the AC of that type<br/>
 * 2) @Resource(name="org.sakaiproject.user.api.UserDirectoryService")<br/>
 * Add the {@link Resource} annotation to any public setter and spring will attempt
 * to locate a bean with the id that matches the name set in the annotation
 * and inject it into the setter, the bean type must match the set type<br/>
 * <b>NOTE:</b> the standard {@link #setUp()} method will be executed AFTER the
 * beans have been injected so it is safe to reference injected beans in it<br/>
 * <br/>
 * There are also a few methods to make it easier to write multithreaded tests
 * which you are welcome to use or ignore
 *
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class SpringTestCase extends TestCase implements ApplicationContextAware {

   protected DecimalFormat df = new DecimalFormat("#,##0.00");
   private static final Log log = LogFactory.getLog(SpringTestCase.class);
   protected static Object compMgr;

   private ApplicationContext applicationContext;
   public void setApplicationContext(ApplicationContext applicationContext) {
      this.applicationContext = applicationContext;
   }

   public ApplicationContext getApplicationContext() {
      return applicationContext;
   }

   /**
    * Easy method for getting single beans out of the application context,
    * will ignore the additional beans and simply provide the first one found
    * @param className the class type of the bean to locate
    * @return the bean object if any can be found (will throw exception if none found)
    */
   public <T extends Object> T getSpringBean(Class<T> className) {
      return getSpringBeans(className).get(0);
   }

   /**
    * Easy method for getting beans out of the application context of a certain type
    * @param className the class type of the bean to locate
    * @return a list of bean objects if any can be found (will throw exception if none found)
    */
   @SuppressWarnings("unchecked")
   public <T extends Object> List<T> getSpringBeans(Class<T> className) {
      String[] beanNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(applicationContext, className);
      if (beanNames.length <= 0) {
         throw new RuntimeException("No beans could be found with the class type: " + className);
      }
      List<T> beans = new ArrayList<T>();
      for (int i = 0; i < beanNames.length; i++) {
         beans.add((T) applicationContext.getBean(beanNames[i]));
      }
      return beans;
   }

   /**
    * A method which makes it easy to calculate a common stat for load testing,
    * microseconds per iteration
    * @param loopCount total number of operations
    * @param totalMilliSecs total number of milliseconds
    * @return the number of microsecs per operation
    */
   public String calcUSecsPerOp(long loopCount, long totalMilliSecs) {
      return df.format(((double)(totalMilliSecs * 1000))/((double)loopCount));
   }

   private Map<String, Date> checkpointMap = new ConcurrentHashMap<String, Date>();

   /**
    * Call this to indicate that this thread should be monitored as part of a series of
    * threads which are running tests (usually to load test a method/service and 
    * see if it can handle multiple threads) by the ThreadMonitor<br/>
    * <b>NOTE:</b> ALl test threads should be daemons
    * @see #startThreadMonitor()
    */
   public void monitoringThread() {
      Thread current = Thread.currentThread();
      if (! checkpointMap.containsKey(current.getName())) {
         //current.setDaemon(true); // all test threads should be daemons
         current.setPriority(Thread.MAX_PRIORITY); // all monitored test threads get top priority
      }
      checkpointMap.put(current.getName(), new Date());
   }

   /**
    * Call this to let the ThreadMonitor know that your test thread is complete<br/>
    * @see #startThreadMonitor()
    */
   public void endMonitoringThread() {
      checkpointMap.remove(Thread.currentThread().getName());
   }

   /**
    * This allows you to run a series of test threads but keep the tests from moving on to other tests until these
    * threads are all completed or deadlocked. Initiates monitoring of the other test threads and block this test 
    * from completing until all test threads complete<br/>
    * You must indicate the threads to be monitored by calling {@link #monitoringThread()} from within
    * the thread which is being monitored when it is first starting, you should also call the method once every
    * 3 seconds from within the thread or it will be logged as slow (this is not a problem but it adds a lot of output
    * and makes it so the monitor cannot tell which threads are in trouble)<br/>
    * When the thread is done with its run then you must call (probably in your finally block) 
    * {@link #endMonitoringThread()} to let the ThreadMonitor know that the thread is successfully complete,
    * if this is not done then the ThreadMonitor will assume the Thread is deadlocked<br/>
    * <br/>
    * <b>Example:</b> this shows one possible way to start up a series of test threads, the runTestThread method
    * would just be a private method in your TestCase which you call, it does not need to have anything
    * special in it except for the {@link #monitoringThread()} and {@link #endMonitoringThread()} calls,
    * however, you can pass information into the method if you want it to scale depending on the number
    * of threads being started (this is what the example here is doing)<br/>
    * <xmp>
      for (int t = 0; t < threads; t++) {
         final int threadnum = t+1;
         Thread thread = new Thread( new Runnable() {
            public void run() {
               setAdminUser();   
               runTestThread(threadnum, threads, threadIterations, threadMaxInserts);
            }
         }, threadnum+"");
         thread.start();
      }
      startThreadMonitor();
    * </xmp>
    */
   public void startThreadMonitor() {
      // monitor the other running threads
      Map<String, Date> m = new HashMap<String, Date>();
      //log.debug("Starting up monitoring of test threads...");
      try {
         Thread.sleep(3 * 1000);
      } catch (InterruptedException e) {
         e.printStackTrace();
      }

      while (true) {
         if (checkpointMap.size() == 0) {
            //log.debug("All test threads complete... monitoring exiting");
            break;
         }
         int deadlocks = 0;
         List<String> stalledThreads = new ArrayList<String>();
         for (String key : checkpointMap.keySet()) {
            if (m.containsKey(key)) {
               if (m.get(key).equals(checkpointMap.get(key))) {
                  double stallTime = (new Date().getTime() - checkpointMap.get(key).getTime()) / 1000.0d;
                  stalledThreads.add(df.format(stallTime) + ":" + key);
                  deadlocks++;
               }
            }
            m.put(key, checkpointMap.get(key));
         }

         if (! stalledThreads.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Deadlocked/slow threads (of "+checkpointMap.size()+"): ");
            sb.append("total="+stalledThreads.size()+":: ");
            Collections.sort(stalledThreads);
            for (int j = stalledThreads.size()-1; j >= 0; j--) {
               String string = stalledThreads.get(j);
               sb.append(string.substring(string.indexOf(':')+1) + "(" + string.substring(0, string.indexOf(':')) + "s):");
            }
            //log.info(sb.toString());
            System.out.println("INFO: " + sb.toString()); // switched to avoid the logger dependency
         }

         try {
            Thread.sleep(3 * 1000);
         } catch (InterruptedException e) {
            e.printStackTrace();
         }
      }
   }
   

	
	/**
	 * Initialize the component manager once for all tests, and log in as admin.
	 */
	
	//TODO: add BeforeClass/AfterClass annotation so that client classes don't have to configure this
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
			
			Class clazz = Class.forName("org.sakaiproject.component.cover.ComponentManager");
			compMgr = clazz.getDeclaredMethod("getInstance", (Class[])null).invoke((Object[])null, (Object[])null);

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
		String home = null;
		try {
			home = System.getProperty("test.tomcat.home");
			if ( null == home || home.trim().length() == 0 ) {
				home = System.getProperty("maven.tomcat.home");
			}
		} catch (SecurityException e){
			log.error("Unable to read maven.tomcat.home or test.tomcat.home properties: SecurityException: ");
			e.printStackTrace();
		}
		if ( null == home || home.trim().length() == 0 ) {
			log.error("Not found: test.tomcat.home or maven.tomcat.home");
		} else {
		    log.info("Using tomcat home: " + home);
		}
		return home;
	}
	//TODO: catch sec exception && test for sakai.home
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
