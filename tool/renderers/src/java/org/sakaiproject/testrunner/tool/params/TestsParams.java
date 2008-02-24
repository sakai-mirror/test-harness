package org.sakaiproject.testrunner.tool.params;

import uk.org.ponder.rsf.viewstate.SimpleViewParameters;

public class TestsParams extends SimpleViewParameters {
    public String testsID;
    
    public TestsParams() {}
    public TestsParams(String viewid, String testsid) {
        this.viewID = viewid;
        this.testsID = testsid;
    }
}
