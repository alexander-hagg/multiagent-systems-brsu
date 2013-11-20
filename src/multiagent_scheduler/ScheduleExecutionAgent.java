package multiagent_scheduler;

import java.util.Vector;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class ScheduleExecutionAgent extends Agent{

	private static final long serialVersionUID = 4853132690502168830L;
	SystemTime ticker;
	private Vector<AID> schedulerAgents = new Vector<AID>();

	protected void setup() {
		System.out.println("ScheduleExecutionAgent "+ getAID().getName() + " is ready.");
		ticker = new SystemTime(this);
		addBehaviour(ticker);
		
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType("scheduling");
		template.addServices(sd);
		try {
			DFAgentDescription[] result = DFService.search( this,
															template);
			schedulerAgents.clear();
			for (int i = 0; i < result.length; ++i) {
				schedulerAgents.addElement(result[i].getName());
			}
		}
		catch (FIPAException fe) {
		fe.printStackTrace();
		}
	}
	
	protected void takeDown() {
		System.out.println("ScheduleExecutionAgent  " + getAID().getName() + " terminating.");
	}
}
