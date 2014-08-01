package org.jenkinsci.plugins.workflow.job;

import hudson.model.Result;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.junit.Test;
import org.junit.runners.model.Statement;
import org.jvnet.hudson.test.JenkinsRule;

/**
 * @author Kohsuke Kawaguchi
 */
public class PersistenceFailureTest extends SingleJobTestBase {
    /**
     *
     */
    @Test
    public void stepExecutionFailsToPersist() throws Exception {
        story.addStep(new Statement() {
            @Override
            public void evaluate() throws Throwable {
                p = jenkins().createProject(WorkflowJob.class, "demo");
                p.setDefinition(new CpsFlowDefinition(join(
                        "with.node {",
                        "  steps.persistenceProblem()",
                        "}"
                )));

                startBuilding();
                waitForWorkflowToSuspend();

                // TODO: let the ripple effect of a failure run to the completion.
                while (b.isBuilding())
                    waitForWorkflowToSuspend();

                Result r = b.getResult();
                String log = JenkinsRule.getLog(b);

                assert r == Result.FAILURE: "Result is "+r+"\n"+ log;
                assert log.contains("java.lang.RuntimeException: testing the forced persistence failure behaviour"): "Result is "+r+"\n"+ log;
            }
        });
        story.addStep(new Statement() {
            @Override
            public void evaluate() throws Throwable {
                rebuildContext(story.j);

                Result r = b.getResult();
                assert r == Result.FAILURE: "Result is "+r+"\n"+JenkinsRule.getLog(b);
            }
        });
    }
}
