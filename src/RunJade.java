import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;

import jade.util.leap.Iterator;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;



/**
 * The Jade's documentation is bad, you can hardly find how to launch a platform from source code.  
 * I hope these few classes will be useful.
 * 
 * Here:  1 main container, 3 containers, 4 agents
 * Agent0 on container0
 * Agent1 and Agent3 on container1
 * Agent2 on container2
 * 
 * Agent0 will send a message to agent1 that's all :)
 * 
 * The "pause" when launching the platform  is just created to let you the possibility to activate the sniffer agent from its GUI in order to sniff the agents and to graphicaly see the message passing.
 * In any case, I chose here to print the sent/received on the standard output.
 * 
 * For sake of simplicity, the creation of the containers and of the agents is hardcoded, and I do not activate the sniffer.  It is bad.
 * 
 * 
 * Tested with Jade 3.7 and Jade 4.0
 * 
 * 
 * @author Cedric Herpson
 *
 */

public class RunJade {

	private static String hostname = "localhost"; 
	private static HashMap<String, ContainerController> containerList=new HashMap<String, ContainerController>();// container's name - container's ref
	private static List<AgentController> agentList;// agents's ref
	private static Runtime rt;	

	public static void main(String[] args){

		rt=emptyPlatform(containerList);

		agentList=createAgents(containerList);

		try {
			System.out.println("Press a key to start the agents");
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}

		startAgents(agentList);

	}


	private static Runtime emptyPlatform(HashMap<String, ContainerController> containerList){

		Runtime rt = Runtime.instance();

		Profile pMain = new ProfileImpl(hostname, 8888, null);
		System.out.println("Launching a main-container..."+pMain);
		AgentContainer mainContainerRef = rt.createMainContainer(pMain); //DF and AMS are include

		containerList.putAll(createContainers(rt)); 

		createMonitoringAgents(mainContainerRef);

		System.out.println("Plaform ok");
		return rt;

	}

	private static HashMap<String,ContainerController> createContainers(Runtime rt) {
		String containerName;
		ProfileImpl pContainer;
		ContainerController containerRef;
		HashMap<String, ContainerController> containerList = new HashMap<String, ContainerController>();//bad to do it here.

		System.out.println("Launching containers ...");

		containerName = "container1";
		pContainer = new ProfileImpl(null, 8888, null);
		System.out.println("Launching container "+pContainer);
		containerRef = rt.createAgentContainer(pContainer);
		containerList.put(containerName, containerRef);

		System.out.println("Launching containers done");
		return containerList;
	}

	private static void createMonitoringAgents(ContainerController mc) {

		System.out.println("Launching the rma agent on the main container ...");
		AgentController rma;

		try {
			rma = mc.createNewAgent("rma", "jade.tools.rma.rma", new Object[0]);
			rma.start();
		} catch (StaleProxyException e) {
			e.printStackTrace();
			System.out.println("Launching of rma agent failed");
		}
	
	}

	private static List<AgentController> createAgents(HashMap<String, ContainerController> containerList) {
		System.out.println("Launching agents...");
		ContainerController c;
		String agentName;
		List<AgentController> agentList = new ArrayList<AgentController>();

		c = containerList.get("container1");
		agentName="JobSupplier1";
		try {					
			Object[] objtab = new Object[]{new String("/home/alex/Documents/FH/MAS/Semester 3/Multiagent/JADE-all-4.3.0/jade/src/multiagent-systems-brsu/jobs")};//used to give informations to the agent
			AgentController	ag = c.createNewAgent(agentName,multiagent_scheduler.JobSupplierAgent.class.getName(),objtab);
			agentList.add(ag);
			System.out.println(agentName + " launched");
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}

		agentName="Scheduler1";
		try {
			Object[] objtab = new Object[]{};//used to give informations to the agent
			AgentController	ag = c.createNewAgent(agentName,multiagent_scheduler.SchedulerAgent.class.getName(),objtab);
			agentList.add(ag);
			System.out.println(agentName + " launched");
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}	

		agentName="SchedulingVisualizer1";
		try {					
			Object[] objtab = new Object[]{};//used to give informations to the agent
			AgentController	ag = c.createNewAgent(agentName,multiagent_scheduler.SchedulingVisualizer.class.getName(),objtab);
			agentList.add(ag);
			System.out.println(agentName + " launched");
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}

		System.out.println("Agents launched...");
		return agentList;
	}

	private static void startAgents(List<AgentController> agentList){

		System.out.println("Starting agents...");

		for(final AgentController ac: agentList){
			try {
				ac.start();
			} catch (StaleProxyException e) {
				e.printStackTrace();
			}

		}
		System.out.println("Agents started...");
	}
}
