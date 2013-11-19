package multiagent_scheduler;

import java.io.IOException;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ScheduleExecutionAgent extends Agent{

	private static final long serialVersionUID = 4853132690502168830L;
	private MessageTemplate templateSchedule;
	int tickTime = 0;

	protected void setup() {
		System.out.println("ScheduleExecutionAgent "+ getAID().getName() + " is ready.");
		
		templateSchedule = MessageTemplate.MatchPerformative( ACLMessage.INFORM ); 
		
		addBehaviour(new CyclicBehaviour(this)
		{
			private static final long serialVersionUID = 8693491533114444273L;
			public void action()  
	         {
	            ACLMessage msg = receive( templateSchedule );
	            if (msg!=null && msg.getContent().equals("tick")) { 
	                tickTime++;
	                System.out.println("Tick " + tickTime + " ScheduleExecutionAgent");
	            }
	            block();
	         }
		});	
	}
	
	protected void takeDown() {
		System.out.println("ScheduleExecutionAgent  " + getAID().getName() + " terminating.");
	}
}
