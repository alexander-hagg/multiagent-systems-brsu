package multiagent_scheduler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class SystemClockAgent extends Agent {

	private static final long serialVersionUID = -2319170144217755318L;
	private AMSAgentDescription[] agents;
	private ACLMessage message;
	protected static int cidCnt = 0;
	String cidBase;

	protected void setup() {
		System.out.println("SystemClockAgent "+ getAID().getName() + " is ready.");
		
		try {
            SearchConstraints c = new SearchConstraints();
            c.setMaxResults (new Long(-1));
            AMSAgentDescription description = new AMSAgentDescription();
			agents = AMSService.search( this, description, c );
		}
		catch ( Exception e ) {
            System.out.println( "Problem searching AMS: " + e );
            e.printStackTrace();
		}

		addBehaviour(new TickSystemBehaviour(this, 1500));	

	}
	
	private class TickSystemBehaviour extends TickerBehaviour
	{

		private static final long serialVersionUID = 2319170146627755318L;
		
		private TickSystemBehaviour(Agent a, int tick) {
			super(a, tick);
		}

		@Override
		protected void onTick() {
			message = new ACLMessage(ACLMessage.INFORM);
			message.setConversationId( genCID() );
			message.setContent("tick");
			for ( AMSAgentDescription agent: agents ) {
				message.addReceiver( agent.getName() );
			}
			send( message );
			block();
		}
	}
	
	protected void takeDown() {
		System.out.println("SystemClockAgent " + getAID().getName() + " terminating.");
	}
	
	protected String genCID() { 
		if (cidBase==null) {
			cidBase = getLocalName() + hashCode() +
                      System.currentTimeMillis()%10000 + "_";
		}
		return cidBase + (cidCnt++); 
	}
}
