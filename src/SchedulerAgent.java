import java.util.ArrayList;
import java.util.Collections;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.*;

public class SchedulerAgent extends Agent {

	private static final long serialVersionUID = -6918861190459111898L;
	protected static int cidCnt = 0;
	String cidBase;
	private ACLMessage msg;
	private MessageTemplate template;
	ArrayList<Job> joblist = new ArrayList<Job>();
	AMSAgentDescription [] agents = null;

	
	protected void setup() {
		System.out.println("SchedulerAgent "+ getAID().getName() + " is ready.");
		
		// Find all other agents
		// TODO: look for JobSupplierAgents only
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
		
		for (AMSAgentDescription agent: agents) {
			msg.addReceiver( agent.getName() );
		}
			 
				
		template = MessageTemplate.and( MessageTemplate.MatchPerformative( ACLMessage.INFORM ),
	            						MessageTemplate.MatchConversationId( msg.getConversationId() ));
				
		SequentialBehaviour seq = new SequentialBehaviour();

		seq.addSubBehaviour( new myReceiver(this, 1000, template )
        {
			private static final long serialVersionUID = 8693491577914569273L;

			public void handle( ACLMessage msg ) 
			{  
				if (msg == null) 
					System.out.println("SchedulerAgent: Timeout");
				else 
					System.out.println("SchedulerAgent received jobs \n=======================");
					try {
						joblist = (ArrayList<Job>) msg.getContentObject();
						for (Job job : joblist)
							System.out.println(job.name + "\n" + job.duration);
					} catch (UnreadableException e) {
						e.printStackTrace();
					}
  
           }
        });
		
		seq.addSubBehaviour( new SimpleBehaviour() {
			
			private static final long serialVersionUID = -7951492880959885049L;
			boolean done = false;
			public boolean done() {
				return done;
			}
			
			@Override
			public void action() {
				Collections.sort(joblist);
				done = true;
			}
        });

		addBehaviour( seq );

      
		send ( msg );
		
	}
	
	protected void takeDown() {
	
		System.out.println("SchedulingVisualizer "+getAID().getName()+" terminating.");
	}
	
	// helper methods
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