package org.sakaiproject.testrunner.tool.beans;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.sakaiproject.testrunner.TestRunnerService;
import org.sakaiproject.testrunner.tool.model.TestRunModel;

public class TestTrackerBean {
    private TestRunnerService testRunnerService;
    
    private List<TestRunModel> testRuns = new ArrayList<TestRunModel>(); 

    public void startTest(String testid) {
        TestRunModel run = new TestRunModel();
        run.testsID = testid;
        run.timeStarted = new Date();
        run.results = testRunnerService.runTests(testid, null);
        run.timeEnded = new Date();
        testRuns.add(run);
    }

    public void setTestRunnerService(TestRunnerService testRunnerService) {
        this.testRunnerService = testRunnerService;
    }

    public List<TestRunModel> getTestRuns() {
        return testRuns;
    }


}