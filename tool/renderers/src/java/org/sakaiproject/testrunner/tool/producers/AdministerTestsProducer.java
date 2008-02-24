package org.sakaiproject.testrunner.tool.producers;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.sakaiproject.testrunner.TestRunnerService;
import org.sakaiproject.testrunner.tool.beans.TestStarterBean;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.DefaultView;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/*
 * The main screen for administering the test runner and launching tests. 
 */
public class AdministerTestsProducer implements DefaultView, ViewComponentProducer,
NavigationCaseReporter {
    public static final String VIEWID = "AdministerTests";
    
    private TestRunnerService testRunnerService; 
    
    public String getViewID() {
        return VIEWID;
    }

    public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
        
        /* Code for enabling and disabling testing */
        if(testRunnerService.isTestingEnabled()) {
            UIOutput.make(tofill, "tests-enabled-message", "Tests are enabled");
        }
        else {
            UIOutput.make(tofill, "tests-enabled-message", "Tests are not enabled");
        }
        
        UIForm enableDisableForm = UIForm.make(tofill, "enable-disable-form");
        if (testRunnerService.isTestingEnabled()) {
            UICommand.make(enableDisableForm, "enable-disable-button", "Disable Testing", 
                    "AdminTestRunnerBean.disableTesting" );
        }
        else {
            UICommand.make(enableDisableForm, "enable-disable-button", "Enable Testing",
                    "AdminTestRunnerBean.enableTesting" );
        }
        
        /* Code for displaying and setting the autodelay */
        int currentAutoDelay = testRunnerService.getAutomaticTestDelaySeconds();
        UIOutput.make(tofill, "autodelay-message", "Autodelay is set to: " 
                + currentAutoDelay + " seconds");
        UIForm autoDelayForm = UIForm.make(tofill, "autodelay-form");
        UIInput.make(autoDelayForm, "delay-input", "AdminTestRunnerBean.autodelay", currentAutoDelay+"" );
        UICommand.make(autoDelayForm, "save-delay-button", "Save Delay Time", "AdminTestRunnerBean.saveAutoDelay");
    
        /* Code for rendering the test buckets */
        List<String> testIDs = testRunnerService.getTestsIds(null);
        for (String testid: testIDs) {
            renderTestBucket(tofill, testid);
        }
    }
    
    private void renderTestBucket(UIContainer tofill, String testid) {
        UIBranchContainer div = UIBranchContainer.make(tofill, "test-bucket-div:");
        UIOutput.make(div, "bucket-id", testid);
        
        UIForm runTestForm = UIForm.make(div, "run-test-form");
        UICommand runTestButton = UICommand.make(runTestForm, "run-test-button", "Run Test", 
                "TestStarterBean.start");
        runTestButton.addParameter(new UIELBinding("TestStarterBean.testid",testid));
        
        List<Class<? extends TestCase>> testcases = testRunnerService.getRegisteredTests(testid, null);
        for (Class<? extends TestCase> testcase : testcases) {
            UIBranchContainer testCaseRow = UIBranchContainer.make(div, "testcase-row:");
            UIOutput.make(testCaseRow, "name", testcase.getName());
        }
    }

    public void setTestRunnerService(TestRunnerService testRunnerService) {
        this.testRunnerService = testRunnerService;
    }

    public List reportNavigationCases() {
        List cases = new ArrayList();
        cases.add(new NavigationCase(TestStarterBean.STARTED_TEST, 
                new SimpleViewParameters(ResultsOverviewProducer.VIEWID)));
        return cases;
    }

}
