/**
 * Autowired.java - 2007 Oct 25, 2007 10:32:19 PM - test-runner - AZ
 */

package org.sakaiproject.testrunner.utils.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * This is just here until we can replace it with the real spring one
 * 
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value={ElementType.METHOD})
public @interface Autowired {
   // this space intentionally left blank
}
