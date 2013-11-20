package multiagent_scheduler;

import jade.core.Agent;

public class ScheduleExecutionAgent extends Agent{

	private static final long serialVersionUID = 4853132690502168830L;
	SystemTime ticker;

	protected void setup() {
		System.out.println("ScheduleExecutionAgent "+ getAID().getName() + " is ready.");
		ticker = new SystemTime(this);
		addBehaviour(ticker);
	}
	
	
	protected void takeDown() {
		System.out.println("ScheduleExecutionAgent  " + getAID().getName() + " terminating.");
	}
}
