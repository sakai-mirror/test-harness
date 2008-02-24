package org.sakaiproject.testrunner.tool.model;

import java.util.Date;
import java.util.Map;

import junit.framework.TestCase;
import junit.framework.TestResult;

public class TestRunModel {
    public String testsID;
    public Date timeStarted;
    public Date timeEnded;
    public Map<Class<? extends TestCase>, TestResult> results;
    public String testType;
    
    /*
     * Returned array looks like
     * 
     * Successful: 0  Failures: 2  Errors: 5 Number of Tests 7
     * 
     * new int [] {0,2,5,7}
     * 
     * Successful: This is 0 or 1 depending on if the whole thing worked. 
     * Failures, Errors, and Number of Tests are raw counts. 
     */
    public int[] getTotalResults() {
        int failures = 0;
        int errors = 0;
        int successful = 1;  // start out as true
        int numtests = 0;
        for (Class<? extends TestCase> testcase: results.keySet()) {
            TestResult testResult = results.get(testcase);
            failures += testResult.failureCount();
            errors += testResult.errorCount();
            numtests += testResult.runCount();
            if (!testResult.wasSuccessful()) {
                successful = 0;
            }
        }
        
        return new int[] {successful,failures,errors,numtests};
        
    }
}
