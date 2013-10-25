import java.util.ArrayList;
import java.util.Collections;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class SchedulingVisualizer extends Agent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6918861190459111898L;
	protected static int cidCnt = 0;
	String cidBase;
	private ACLMessage msg;
	private MessageTemplate template;
	AMSAgentDescription [] agents = null;
	ArrayList<Integer> schedule = new ArrayList<Integer>();

	
	//private AID[] sellerAgents = {new AID("seller1", AID.ISLOCALNAME),
	
	protected void setup() {
		System.out.println("SchedulingVisualizer "+getAID().getName()+" is ready.");
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
		
		msg = newMsg( ACLMessage.QUERY_REF );
		msg.setContent("schedule");
		
		for (AMSAgentDescription agent: agents) {
			msg.addReceiver( agent.getName() );
		}
		
		template = MessageTemplate.and( MessageTemplate.MatchPerformative( ACLMessage.INFORM ),
				MessageTemplate.MatchConversationId( msg.getConversationId() ));

		SequentialBehaviour seq = new SequentialBehaviour();
		
		seq.addSubBehaviour( new myReceiver(this, 1000, template )
			{
				private static final long serialVersionUID = 8693553115914569273L;

				public void handle( ACLMessage msg ) 
				{  
					if (msg == null) 
						System.out.println("SchedulerAgentVisualizer: Timeout");
					else 
						System.out.println("SchedulerAgentVisualizer received schedule \n=======================");
					try {
						schedule = (ArrayList<Integer>) msg.getContentObject();						
					} catch (UnreadableException e) {
						e.printStackTrace();
					}
				}
		});
		
		seq.addSubBehaviour( new SimpleBehaviour() {
			boolean done = false;
			public boolean done() {
				return done;
			}
		
			@Override
			public void action() {
				// TODO build graphical representation of received schedule(s)
				System.out.println("SCHEDULE:\n");
				for (int job : schedule)
					System.out.println(job + "\n");
				done = true;
			}
		});
		
		addBehaviour( seq );
		send ( msg );
	}
	
	protected void takeDown() {
	
		System.out.println("SchedulingVisualizer "+getAID().getName()+" terminating.");
	}

	protected String genCID() { 
		if (cidBase==null) {
			cidBase = getLocalName() + hashCode() +
                      System.currentTimeMillis()%10000 + "_";
		}
		return cidBase + (cidCnt++); 
	}
	
	protected ACLMessage getMessage() { return msg; }
	
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