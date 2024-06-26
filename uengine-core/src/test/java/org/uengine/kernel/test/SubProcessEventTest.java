package org.uengine.kernel.test;

import org.uengine.kernel.DefaultActivity;
import org.uengine.kernel.ExecutionScopeContext;
import org.uengine.kernel.ProcessDefinition;
import org.uengine.kernel.ProcessInstance;
import org.uengine.kernel.ProcessVariable;
import org.uengine.kernel.ProcessVariableValue;
import org.uengine.kernel.ReceiveActivity; // Assuming this import is necessary for ReceiveActivity
import org.uengine.kernel.bpmn.Event;
import org.uengine.kernel.bpmn.SequenceFlow;
import org.uengine.kernel.bpmn.StartEvent;
import org.uengine.kernel.bpmn.SubProcess;

public class SubProcessEventTest extends UEngineTest {

    ProcessDefinition processDefinition;

    public void setUp() throws Exception {
        processDefinition = new ProcessDefinition();

        // 서브프로세스 및 이벤트 구성
        SubProcess subProcess = new SubProcess();
        subProcess.setTracingTag("subProcess");

        // Step 1: Declare a ProcessVariable
        ProcessVariable myVariable = new ProcessVariable();
        myVariable.setName("myVar");

        // Assuming you have a process instance 'instance'
        // Step 3: Set the ProcessVariableValue to the process instance
        // This step will be assumed to be done when the instance is created and
        // executed

        // Step 4: Set the ProcessVariable as the forEachVariable of the SubProcess
        subProcess.setForEachVariable(myVariable);

        Event startEvent = new StartEvent();
        startEvent.setTracingTag("startEvent");
        // startEvent를 processDefinition의 첫 번째 요소로 추가
        processDefinition.addChildActivity(startEvent);

        // subProcess를 processDefinition의 두 번째 요소로 추가
        processDefinition.addChildActivity(subProcess);

        Event cancelEvent = new Event();
        cancelEvent.setTracingTag("cancelEvent");
        cancelEvent.setAttachedToRef("subProcess");
        // cancelEvent를 processDefinition의 요소로 추가
        processDefinition.addChildActivity(cancelEvent);

        // DefaultActivity를 ReceiveActivity로 변경
        ReceiveActivity activityWithinSubProcess = new ReceiveActivity();
        activityWithinSubProcess.setTracingTag("activityWithinSubProcess");
        activityWithinSubProcess.setMessage("receive"); // 이벤트를 받기 위한 설정
        subProcess.addChildActivity(activityWithinSubProcess);

        DefaultActivity activityAfterSubProcess = new DefaultActivity();
        activityAfterSubProcess.setTracingTag("activityAfterSubProcess");
        processDefinition.addChildActivity(activityAfterSubProcess);

        // 시퀀스 플로우 구성
        processDefinition.addSequenceFlow(new SequenceFlow("startEvent", "subProcess"));
        processDefinition.addSequenceFlow(new SequenceFlow("subProcess", "activityAfterSubProcess"));

        processDefinition.afterDeserialization();
    }

    public void testCancelEventWithinSubProcess() throws Exception {
        ProcessInstance instance = processDefinition.createInstance();

        // Step 2: Create a ProcessVariableValue instance and populate it
        ProcessVariableValue pvv = new ProcessVariableValue();
        pvv.setName("myVar");
        pvv.setValue("value1");
        pvv.moveToAdd();
        pvv.setValue("value2");
        pvv.moveToAdd();
        pvv.setValue("value3");

        // Step 3: Set the ProcessVariableValue to the process instance
        instance.set("", "myVar", pvv);
        instance.execute();
        // 서브프로세스 내에서 취소 이벤트 발생
        instance.getProcessDefinition().fireMessage("event", instance, "cancelEvent");

        assertExecutionPathEquals("After Cancel Event Triggered Within SubProcess", new String[] {
                "startEvent", "cancelEvent"
        }, instance);

        // 서브프로세스 내의 ReceiveActivity를 트리거하여 진행
        ExecutionScopeContext rootExecutionScopeContext = instance.getExecutionScopeContext();

        for (ExecutionScopeContext esc : instance.getExecutionScopeContexts()) {
            instance.setExecutionScope(esc.getExecutionScope());
            instance.getProcessDefinition().fireMessage("receive", instance, null);
            instance.getProcessTransactionContext().commit();
        }

        if (rootExecutionScopeContext != null) {
            instance.setExecutionScope(rootExecutionScopeContext.getExecutionScope());
        } else {
            instance.setExecutionScope(null);
        }

        assertExecutionPathEquals("After Receive Event Triggered Within SubProcess", new String[] {
                "startEvent", "cancelEvent", "activityWithinSubProcess", "activityWithinSubProcess",
                "activityWithinSubProcess", "subProcess", "activityAfterSubProcess", "subProcess",
                "activityAfterSubProcess", "subProcess", "activityAfterSubProcess"
        }, instance);
    }
}