package me.kafeitu.activiti.chapter2;

import org.activiti.engine.*;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import javax.xml.ws.Holder;

import static org.junit.Assert.*;

public class SayHelloToLeaveTest {

    @Test
    public void testStartProcess() throws Exception {
        ProcessEngine processEngine = ProcessEngineConfiguration
                .createStandaloneInMemProcessEngineConfiguration()
                .buildProcessEngine();

        RepositoryService repositoryService = processEngine.getRepositoryService();
        String bpmnFileName = "me/kafeitu/activiti/helloworld/SayHelloToLeave.bpmn";
        repositoryService
                .createDeployment()
                .addInputStream(
                        "SayHelloToLeave.bpmn",
                        this.getClass().getClassLoader()
                                .getResourceAsStream(bpmnFileName)).deploy();

        ProcessDefinition processDefinition = repositoryService
                .createProcessDefinitionQuery().singleResult();
        assertEquals("SayHelloToLeave", processDefinition.getKey());

        RuntimeService runtimeService = processEngine.getRuntimeService();

        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("applyUser", "employee1");
        variables.put("days", 3);

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
                "SayHelloToLeave", variables);
        assertNotNull(processInstance);
        System.out.println("pid=" + processInstance.getId() + ", pdid="
                + processInstance.getProcessDefinitionId());

        TaskService taskService = processEngine.getTaskService();
        Task taskOfDeptLeader = taskService.createTaskQuery()
                .taskCandidateGroup("deptLeader").singleResult();
        assertNotNull(taskOfDeptLeader);
        assertEquals("领导审批", taskOfDeptLeader.getName());

        taskService.claim(taskOfDeptLeader.getId(), "leaderUser");
        variables = new HashMap<String, Object>();
        variables.put("approved", true);
        taskService.complete(taskOfDeptLeader.getId(), variables);

        taskOfDeptLeader = taskService.createTaskQuery()
                .taskCandidateGroup("deptLeader").singleResult();
        assertNull(taskOfDeptLeader);

        HistoryService historyService = processEngine.getHistoryService();
        long count = historyService.createHistoricProcessInstanceQuery().finished()
                .count();
        assertEquals(1, count);
    }
    
    @Test
    public void testStartProcess2(){
    	//1.获取流程引擎，设置数据库
    	ProcessEngine processEngine = ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration().buildProcessEngine();
    	
    	//2.发布流程定义文件
    	RepositoryService repositoryService = processEngine.getRepositoryService();
    	String bpmnFileName = "me/kafeitu/activiti/helloworld/SayHelloToLeave2.bpmn";
    	repositoryService.createDeployment().addInputStream("SayHelloToLeave5.bpmn", this.getClass().getClassLoader().getResourceAsStream(bpmnFileName)).deploy();
    	
    	//3.获取流程定义
    	ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    	assertEquals("SayHelloToLeave", processDefinition.getKey());
    	
    	//4.启动流程实例
    	Map var = new HashMap<String, Object>();
    	var.put("applyUser", "王五");
    	var.put("days", 3);
    	RuntimeService runtimeService = processEngine.getRuntimeService();
    	ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("SayHelloToLeave",var);
    	assertNotNull(processInstance);
    	System.out.println("pid=:"+processInstance.getId()+",pdid="+processInstance.getProcessDefinitionId());
    	
    	//5.获取任务
    	TaskService taskService = processEngine.getTaskService();
    	Task task = taskService.createTaskQuery().taskCandidateGroup("deptLeader").singleResult();
    	//6.处理任务
    	taskService.claim(task.getId(), "张三");
    	
    	//7.完成审批
    	var = new HashMap<String, Object>();
    	var.put("approved", true);
    	taskService.complete(task.getId(),var);
    	//8.获取完成流程的实例数量
    	HistoryService historyService = processEngine.getHistoryService();
    	long count = historyService.createHistoricProcessInstanceQuery().finished().count();
    	assertEquals(1, count);
    }
    
}