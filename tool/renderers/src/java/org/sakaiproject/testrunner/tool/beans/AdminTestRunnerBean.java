package org.sakaiproject.testrunner.tool.beans;

import org.sakaiproject.testrunner.TestRunnerService;

public class AdminTestRunnerBean {
    private TestRunnerService testRunnerService;
    
    public int autodelay;
    
    public void init() {
        autodelay = testRunnerService.getAutomaticTestDelaySeconds();
    }
    
    public void disableTesting() {
        testRunnerService.setTestingEnabled(false);
    }
    
    public void enableTesting() {
        testRunnerService.setTestingEnabled(true);
    }
    
    public void saveAutoDelay() {
        testRunnerService.setAutomaticTestDelay(autodelay);
    }

    public void setTestRunnerService(TestRunnerService testRunnerService) {
        this.testRunnerService = testRunnerService;
    }
}