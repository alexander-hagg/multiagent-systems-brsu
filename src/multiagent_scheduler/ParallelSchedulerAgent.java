package multiagent_scheduler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.*;

public class ParallelSchedulerAgent extends Agent {

	private static final long serialVersionUID = -6918861190459111898L;
	protected static int cidCnt = 0;
	String cidBase;
	private ACLMessage jobQuery, reply;
	ArrayList<Job> joblist = new ArrayList<Job>();
	ArrayList<Job> schedule = new ArrayList<Job>();
	AMSAgentDescription [] agents = null;

	
	protected void setup() {
		System.out.println("SchedulerAgent "+ getAID().getName() + " is ready.");
		
		try {
            SearchConstraints c = new SearchConstraints();
            c.setMaxResults ( new Long(-1) );
            AMSAgentDescription description = new AMSAgentDescription();
			agents = AMSService.search( this, description, c );
		}
		catch ( Exception e ) {
            System.out.println( "Problem searching AMS: " + e );
            e.printStackTrace();
		}
		
		jobQuery = newMsg( ACLMessage.QUERY_REF );
		jobQuery.setConversationId(genCID());
		jobQuery.setContent("joblist?");
		for ( AMSAgentDescription agent: agents ) {
			jobQuery.addReceiver( agent.getName() );
		}
		
		addBehaviour( new JobListQuery() );
		addBehaviour( new ScheduleServer() );
		
		send ( jobQuery );
	}
	
	
	private class JobListQuery extends CyclicBehaviour {
		
		private static final long serialVersionUID = -3832723334788838104L;
		private MessageTemplate templateJobList = MessageTemplate.and( MessageTemplate.MatchPerformative( ACLMessage.INFORM ),
																	   MessageTemplate.MatchConversationId(jobQuery.getConversationId()) );

		@SuppressWarnings("unchecked")
		public void action() {
			ACLMessage msg = receive( templateJobList );
			if (msg != null) {
				try {
					joblist = (ArrayList<Job>) msg.getContentObject();
					Collections.sort( joblist );
					for ( Job job : joblist ) {
						schedule.add(job);
					}
				} catch ( UnreadableException e ) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private class ScheduleServer extends CyclicBehaviour {
		
		private static final long serialVersionUID = -3832722334788838104L;
		private MessageTemplate templateSchedule = MessageTemplate.MatchPerformative( ACLMessage.QUERY_REF );

		public void action()  
        {
           ACLMessage msg = receive( templateSchedule );
           if (msg!=null&& msg.getContent().equals( "schedule" )) { 
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
        }
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
	
	protected ACLMessage getMessage() { return jobQuery; }
	
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