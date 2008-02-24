package org.sakaiproject.testrunner.utils.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is just here until we can replace it with the real spring one
 * 
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value={ElementType.METHOD})
public @interface Resource {
   String name();
}
