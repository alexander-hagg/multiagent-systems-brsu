package multiagent_scheduler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class ScheduleExecutionAgent extends Agent{

	private static final long serialVersionUID = 4853132690502168830L;
	SystemTime ticker;
	private Vector<AID> schedulerAgents = new Vector<AID>();
	ACLMessage subscribeQuery;
	private String cidBase;
	protected static int cidCnt = 0;
	boolean subscribed = false;

	protected void setup() {
		System.out.println("ScheduleExecutionAgent "+ getAID().getName() + " is ready.");
		ticker = new SystemTime(this);
		addBehaviour(ticker);
		
		// find scheduler service
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
		
		addBehaviour( new SubscribeQuery() );
		
		
	}
	
	protected void takeDown() {
		System.out.println("ScheduleExecutionAgent  " + getAID().getName() + " terminating.");
	}
	
	private class SubscribeQuery extends CyclicBehaviour {
		
		private static final long serialVersionUID = -3832723334788838104L;
		private MessageTemplate templateSubscriptionSuccess;
		public void action() {
			// subscribe to scheduler service
			if (!subscribed ) {
				subscribeQuery = newMsg( ACLMessage.SUBSCRIBE );
				subscribeQuery.setContent("scheduler");
				for ( AID agent: schedulerAgents ) {
					subscribeQuery.addReceiver( agent );
				}
				templateSubscriptionSuccess = MessageTemplate.and( MessageTemplate.MatchPerformative( ACLMessage.CONFIRM ),
						   MessageTemplate.MatchConversationId(subscribeQuery.getConversationId()) );
				send ( subscribeQuery );
				ACLMessage msg = receive( templateSubscriptionSuccess );
				if (msg != null) {
					subscribed = true;
				}
			}
		}
	}
	
	protected String genCID() { 
		if (cidBase==null) {
			cidBase = getLocalName() + hashCode() +
                      System.currentTimeMillis()%10000 + "_";
		}
		return cidBase + (cidCnt++); 
	}
	
	ACLMessage newMsg( int perf, String content, AID dest) {
		ACLMessage msg = newMsg(perf);
		if (dest != null) msg.addReceiver( dest );
			msg.setContent( content );
		return msg;
	}

	ACLMessage newMsg( int perf) {
		ACLMessage msg = new ACLMessage(perf);
		msg.setConversationId( genCID() );
		return msg;
	}
	
}
