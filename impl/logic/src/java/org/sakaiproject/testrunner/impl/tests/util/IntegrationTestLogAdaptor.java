package org.sakaiproject.testrunner.impl.tests.util;


/*
 * If test-executing classes (testservice, test executor, etc)
 * are running in maven, the recommended log level for projects is INFO.
 * So, in order facilitate log outs being seen while in maven, this class can 'convert'
 * 'info' method calls to 'warn'.
 * 
 * No effective action is taken if this is being run in an appserver classloader 
 *   (eg, *not* running in maven)
 *   
 *   Best way to use this class is to define your commons Log field as as non static member
 *   which is initialized with LogFactory in the normal fashion. Then define your primary logger
 *   field as a type IntegrationTestLogAdaptor. Ideally, initialize this LogAdaptor field only 
 *   after the class is fully instantiated. Then use that field as a normal Log field.
 *   
 *   Example:
 *   private Log commonsLogger = LogFactory.getLog(WorkingClass.class);
 *   private IntegrationTestLogAdaptor log = null;
 *   
 *   .
 *   .
 *   public void init(){
 *   .
 *   .// you'll have to use commonsLogger up to this point 
 *   log = new IntegrationTestLogAdaptor(this, true);
 *    ...
 */


import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class IntegrationTestLogAdaptor implements Log {
		
	private final static Log localLog = LogFactory.getLog(IntegrationTestLogAdaptor.class);
	
	private final static int INFO = 0;
	private final static int INFO2 = 1;
	private final static int DEBUG = 2;
	private final static int DEBUG2 = 3;
	private final static int ERROR = 4;
	private final static int ERROR2 = 5;
	private final static int FATAL = 6;
	private final static int FATAL2 = 7;
	private final static int DEBUG_ENABLED = 8;
	private final static int ERROR_ENABLED = 9;
	private final static int FATAL_ENABLED = 10;
	private final static int INFO_ENABLED = 11;
	private final static int TRACE_ENABLED = 12;
	private final static int WARN_ENABLED = 13;
	private final static int TRACE = 14;
	private final static int TRACE2 = 15;
	private final static int WARN = 16;
	private final static int WARN2 = 17;
	
	private Log callerLog = null;
	
	private Object caller = null;
	
	private boolean switchInfoToWarn = false;
	private Method[] methods = null;
	
	public IntegrationTestLogAdaptor(Object caller, boolean switchInfoToWarn) {
		this.caller = caller;
		this.switchInfoToWarn = switchInfoToWarn;
		Field[] fields = caller.getClass().getDeclaredFields();
		
		Field logField = null;
		for (int i=0;i<fields.length;++i){
			Class<?> type = fields[i].getType();
			Class<?>[] itfs = type.getInterfaces();
			//System.out.println(caller + "." + fields[i].getName()+ " is type " + type);
			if(type.toString().equals("interface org.apache.commons.logging.Log")){
				if(logField != null) {
					localLog.warn("Ambiguousness in Log fields for caller class ("+caller + ")");
				}
				logField = fields[i];
			}

		}
		
		if(null==logField){
			localLog.error("Can't locate field of type Log in caller: " +caller.getClass().getName());
		}else{
			try {
				logField.setAccessible(true);
				callerLog = (Log) logField.get(caller);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} 
			
			if(null==callerLog){
				localLog.error("The Log field (" +logField.getName()+ ") is null");
			}else{
				setMethodPermissions(callerLog.getClass());
				debug("LogAdaptor will use caller's log in field named: " + logField.getName());
			}
		}
		
	}
	

	/*
	 * These two methods realize this class' purpose in life:
	 *   they conditionally delegate to Log 
	 * 
	 */ 
	public void info(Object arg0) {

		if(switchInfoToWarn){
			invokeLogMethod(callerLog, methods[WARN], new Object[] {arg0});
		}else{
			invokeLogMethod(callerLog, methods[INFO], new Object[] {arg0});
		}
	}

	public void info(Object arg0, Throwable arg1) {

		if(switchInfoToWarn){
			invokeLogMethod(callerLog, methods[WARN2], new Object[] {arg0});
		}else{
			invokeLogMethod(callerLog, methods[INFO2], new Object[] {arg0, arg1});
		}
	}

	/*
	 * The rest of these are directly delegated to Log
	 */

	public void debug(Object arg0) {
		invokeLogMethod(callerLog, methods[DEBUG], new Object[] {arg0});
	}
	

	public void debug(Object arg0, Throwable arg1) {
		invokeLogMethod(callerLog, methods[DEBUG2], new Object[] {arg0, arg1});
	}

	public void error(Object arg0) {
		invokeLogMethod(callerLog, methods[ERROR], new Object[] {arg0});
	}

	public void error(Object arg0, Throwable arg1) {
		invokeLogMethod(callerLog, methods[ERROR2], new Object[] {arg0, arg1});
	}

	public void fatal(Object arg0) {
		invokeLogMethod(callerLog, methods[FATAL], new Object[] {arg0});
	}

	public void fatal(Object arg0, Throwable arg1) {
		invokeLogMethod(callerLog, methods[FATAL2], new Object[] {arg0, arg1});
	}


	public boolean isDebugEnabled() {
		return invokeLogMethod(callerLog, methods[DEBUG_ENABLED], new Object[] {});
	}

	public boolean isErrorEnabled() {
		return invokeLogMethod(callerLog, methods[DEBUG_ENABLED], new Object[] {});
	}

	public boolean isFatalEnabled() {
		return invokeLogMethod(callerLog, methods[FATAL_ENABLED], new Object[] {});
	}

	public boolean isInfoEnabled() {
		return invokeLogMethod(callerLog, methods[INFO_ENABLED], new Object[] {});
	}

	public boolean isTraceEnabled() {
		return invokeLogMethod(callerLog, methods[TRACE_ENABLED], new Object[] {});
	}

	public boolean isWarnEnabled() {
		return invokeLogMethod(callerLog, methods[WARN_ENABLED], new Object[] {});
	}

	public void trace(Object arg0) {
		invokeLogMethod(callerLog, methods[TRACE], new Object[] {arg0});
	}

	public void trace(Object arg0, Throwable arg1) {
		invokeLogMethod(callerLog, methods[TRACE2], new Object[] {arg0, arg1});
	}

	public void warn(Object arg0) {
		invokeLogMethod(callerLog, methods[WARN], new Object[] {arg0});
	}

	public void warn(Object arg0, Throwable arg1) {
		invokeLogMethod(callerLog, methods[WARN2], new Object[] {arg0, arg1});
	}
	
	private void setMethodPermissions(Class<?> logClass){
		
		methods = new Method[18];
		try {
			methods[INFO] = logClass.getDeclaredMethod("info", new Class[] {Object.class});
			methods[INFO2] = logClass.getDeclaredMethod("info", new Class[] {Object.class, Throwable.class});
			methods[DEBUG] = logClass.getDeclaredMethod("debug", new Class[] {Object.class});
			methods[DEBUG2] = logClass.getDeclaredMethod("debug", new Class[] {Object.class, Throwable.class});
			methods[ERROR] = logClass.getDeclaredMethod("error", new Class[] {Object.class});
			methods[ERROR2] = logClass.getDeclaredMethod("error", new Class[] {Object.class, Throwable.class});
			methods[FATAL] = logClass.getDeclaredMethod("fatal", new Class[] {Object.class});
			methods[FATAL2] = logClass.getDeclaredMethod("fatal", new Class[] {Object.class, Throwable.class});
			methods[DEBUG_ENABLED] = logClass.getDeclaredMethod("isDebugEnabled", new Class[] {});
			methods[ERROR_ENABLED] = logClass.getDeclaredMethod("isErrorEnabled", new Class[] {});
			methods[FATAL_ENABLED] = logClass.getDeclaredMethod("isFatalEnabled", new Class[] {});
			methods[INFO_ENABLED] = logClass.getDeclaredMethod("isInfoEnabled", new Class[] {});
			methods[TRACE_ENABLED] = logClass.getDeclaredMethod("isTraceEnabled", new Class[] {});
			methods[WARN_ENABLED] = logClass.getDeclaredMethod("isWarnEnabled", new Class[] {});
			methods[TRACE] = logClass.getDeclaredMethod("trace", new Class[] {Object.class});
			methods[TRACE2] = logClass.getDeclaredMethod("trace", new Class[] {Object.class, Throwable.class});
			methods[WARN] = logClass.getDeclaredMethod("warn", new Class[] {Object.class});
			methods[WARN2] = logClass.getDeclaredMethod("warn", new Class[] {Object.class, Throwable.class});
			
			for(int i=0;i<methods.length;++i){
				methods[i].setAccessible(true);
			}
			
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private boolean invokeLogMethod(Object object, Method method, Object[] args){

		boolean rv = false;
		Object ro = null;

		try {

			ro = method.invoke(object, args);
			if(ro!=null){
				rv = ((Boolean)ro).booleanValue();
			}
			
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 


		return rv;
	}

}

