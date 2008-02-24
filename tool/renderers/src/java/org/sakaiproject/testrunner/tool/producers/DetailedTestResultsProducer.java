package org.sakaiproject.testrunner.tool.producers;

import org.sakaiproject.testrunner.tool.params.TestsParams;

import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

public class DetailedTestResultsProducer implements ViewComponentProducer, ViewParamsReporter {
    public static final String VIEWID = "DetailedTestResults";
    
    public String getViewID() {
        return VIEWID;
    }

    public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
        // TODO Auto-generated method stub
        
    }

    public ViewParameters getViewParameters() {
        return new TestsParams();
    }

}
