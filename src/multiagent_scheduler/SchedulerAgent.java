package multiagent_scheduler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.states.MsgReceiver;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.*;

public class SchedulerAgent extends Agent {

	private static final long serialVersionUID = -6918861190459111898L;
	protected static int cidCnt = 0;
	String cidBase;
	private ACLMessage message, reply;
	private MessageTemplate templateJoblist, templateSchedule;
	ArrayList<Job> joblist = new ArrayList<Job>();
	ArrayList<Job> schedule = new ArrayList<Job>();
	AMSAgentDescription [] agents = null;

	
	protected void setup() {
		System.out.println("SchedulerAgent "+ getAID().getName() + " is ready.");
		
		// Find all other agents
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
		
		message = newMsg( ACLMessage.QUERY_REF );
		message.setContent("joblist");
		
		for (AMSAgentDescription agent: agents) {
			message.addReceiver( agent.getName() );
		}
			 
				
		templateJoblist = MessageTemplate.and( MessageTemplate.MatchPerformative( ACLMessage.INFORM ),
	            						MessageTemplate.MatchConversationId( message.getConversationId() ));
				
		SequentialBehaviour seq = new SequentialBehaviour();
		
		Object test = null;
		seq.addSubBehaviour( new MsgReceiver(this, templateJoblist, 1000, null, test )
        {
			private static final long serialVersionUID = 8693491577914569273L;

			@SuppressWarnings("unchecked")
			public void handleMessage( ACLMessage msg ) 
			{  
				if (msg == null) 
					System.out.println("SchedulerAgent: Timeout");
				else 
					try {
						joblist = (ArrayList<Job>) msg.getContentObject();
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
				for (Job job : joblist) {
					schedule.add(job);
				}
				done = true;
			}
        });

		addBehaviour( seq );

		templateSchedule = MessageTemplate.MatchPerformative( ACLMessage.QUERY_REF );
		
		
		addBehaviour(new CyclicBehaviour(this)
		{
			private static final long serialVersionUID = 8693491533424444273L;

			public void action()  
	         {
	            ACLMessage msg = receive( templateSchedule );
	            if (msg!=null&& msg.getContent().equals("schedule")) { 
	                reply = msg.createReply();
	                reply.setConversationId(genCID());
	                reply.setPerformative( ACLMessage.INFORM );

	                try {
	                	
						reply.setContentObject(schedule);
					} catch (IOException e) {
						e.printStackTrace();
					}
	                send(reply);
	            }
	            block();
	         }
		});	
		send ( message );
	}
	
	protected void takeDown() {
		System.out.println("SchedulingVisualizer " + getAID().getName() + " terminating.");
	}
	
	protected String genCID() { 
		if (cidBase==null) {
			cidBase = getLocalName() + hashCode() +
                      System.currentTimeMillis()%10000 + "_";
		}
		return cidBase + (cidCnt++); 
	}
	
	protected ACLMessage getMessage() { return message; }
	
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