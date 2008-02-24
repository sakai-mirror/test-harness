package org.sakaiproject.testrunner.tool.beans;

public class TestStarterBean {
    public static final String STARTED_TEST = "started_test";
    private TestTrackerBean tracker;
    
    public String testid;
    
    public String start() {
        tracker.startTest(testid);
        return STARTED_TEST;
    }
    
    public void setTracker(TestTrackerBean tracker) {
        this.tracker = tracker;
    }
}