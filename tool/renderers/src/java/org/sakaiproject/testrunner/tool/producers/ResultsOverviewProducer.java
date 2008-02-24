package org.sakaiproject.testrunner.tool.producers;

import org.sakaiproject.testrunner.tool.beans.TestTrackerBean;
import org.sakaiproject.testrunner.tool.model.TestRunModel;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;

public class ResultsOverviewProducer implements ViewComponentProducer {
    public static final String VIEWID = "ResultsOverview";

    private TestTrackerBean testTracker;
    
    public String getViewID() {
        return VIEWID;
    }

    public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
        
        for (TestRunModel test : testTracker.getTestRuns()) {
            UIBranchContainer row = UIBranchContainer.make(tofill, "testrun-row:");
            UIOutput.make(row, "testid", test.testsID);
            
            if (test.timeStarted == null) 
                UIOutput.make(row, "started", "Not Started");
            else
                UIOutput.make(row, "started", test.timeStarted.toLocaleString());
            
            if (test.timeEnded == null) 
                UIOutput.make(row, "finished", "Not Finished");
            else
                UIOutput.make(row, "finished", test.timeEnded.toLocaleString());
            
            //if (test.timeStarted != null && test.timeEnded != null) {
                UIOutput.make(row, "running-time", "");
            //}
            
            int[] results = test.getTotalResults();
            
            if (results[0] == 1)
                UIOutput.make(row, "successful", "Yes");
            else
                UIOutput.make(row, "successful", "No");
            
            UIOutput.make(row, "failures", results[1]+"");
            UIOutput.make(row, "errors", results[2]+"");
            UIOutput.make(row, "num-tests", results[3]+"");
        }
        
    }

    public void setTestTracker(TestTrackerBean testTracker) {
        this.testTracker = testTracker;
    }

}
