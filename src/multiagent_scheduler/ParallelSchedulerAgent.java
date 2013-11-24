package multiagent_scheduler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.domain.AMSService;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.*;

public class ParallelSchedulerAgent extends Agent {

	private static final long serialVersionUID = -6918861190459111898L;
	protected static int cidCnt = 0;
	String cidBase;
	private ACLMessage jobQuery, reply;
	ArrayList<Job> joblist = new ArrayList<Job>();
	ArrayList<Job> schedule = new ArrayList<Job>();
	AMSAgentDescription [] agents = null;
	Vector<AID> subscribers = new Vector<AID>();
	
	protected void setup() {
		System.out.println("SchedulerAgent "+ getAID().getName() + " is ready.");
		
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("scheduling");
		sd.setName(getLocalName()+"-scheduling");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
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
		addBehaviour( new SubscriptionServer() );
		addBehaviour( new PublishSchedule() );
		
		send ( jobQuery );
	}
	
	
	private class PublishSchedule extends CyclicBehaviour {
		
		private static final long serialVersionUID = -3832723334788838102L;
		public void action() {
		    int executionAgents = subscribers.size();
		    int totalNumberOfjobs = schedule.size();
		    if (totalNumberOfjobs > 0 && executionAgents > 0) {
		        ArrayList<Job> scheduleCopy = new ArrayList<Job>();
    		    int totalScheduleTime = 0;
    	        for (Job index : schedule) {
    	            totalScheduleTime += index.getProcessingTime();
    	            scheduleCopy.add(index);
    	        }
    	        //int averageLoad = totalScheduleTime/executionAgents;
    			// subscribe to scheduler service
    	        //if ( !subscribers.isEmpty() ) {
    	            System.out.println("AGENTS: " + executionAgents);
    				for ( AID agent: subscribers ) {
    				    ArrayList<Job> subSchedule = new ArrayList<Job>();
    				    //int currentLoad = 0;
    					ACLMessage scheduleMessage = newMsg( ACLMessage.PROPAGATE );
    					try {
    					    for (int i = 0; i < totalNumberOfjobs/executionAgents; i++) {
    					        subSchedule.add(scheduleCopy.get(0));
    					        scheduleCopy.remove(0);
    					        //currentLoad += subSchedule.get(i).getProcessingTime();
    					        //if (currentLoad >= averageLoad) {
    					        //    i = totalNumberOfjobs;
    					        //}
    					    }
    						scheduleMessage.setContentObject(subSchedule);
    					} catch (IOException e) {
    						e.printStackTrace();
    					}
    					scheduleMessage.addReceiver( agent );
    					send( scheduleMessage );
    				}
    			//}
		    }
		    block();
		}
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
	
	private class SubscriptionServer extends CyclicBehaviour {
		
		private static final long serialVersionUID = -3832723334788838104L;
		private MessageTemplate templateSubscriptionQuery = MessageTemplate.and( MessageTemplate.MatchPerformative( ACLMessage.SUBSCRIBE ),
																	   MessageTemplate.MatchContent("scheduler") );

		public void action() {
			ACLMessage msg = receive( templateSubscriptionQuery );
			if (msg != null) {
				try {
					if (!subscribers.contains(msg.getSender())) {
						subscribers.add(msg.getSender());
						System.out.println("Added subscriber to schedule: " + msg.getSender());
					}
				} catch (Exception e){
					System.out.println(e);
				}
			}
		}
	}
	
	protected void takeDown() {
		System.out.println("SchedulingVisualizer " + getAID().getName() + " terminating.");
		try {
			DFService.deregister(this);
		}
			catch (FIPAException fe) {
			fe.printStackTrace();
		}
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