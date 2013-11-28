package multiagent_scheduler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.domain.AMSService;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.*;

/**
 * @author Alexander Hagg
 *
 */
public class ParallelSchedulerAgent extends Agent {
	
	private static final long serialVersionUID = -6918861190459111898L;
	
	protected static int cidCnt = 0;
	String cidBase;
	private ACLMessage jobQuery;
	private boolean joblistReceived = false;
	
	ArrayList<Job> joblist = new ArrayList<Job>();
	ArrayList<ArrayList<Job>> schedule = new ArrayList<ArrayList<Job>>();
	Vector<AID> subscribers = new Vector<AID>();
	AMSAgentDescription [] agents = null;
	
	@Override
	protected void setup() 
	{
		System.out.println( "SchedulerAgent "+ getAID().getName() + " is ready." );
		
		// Register service to DFService
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName( getAID() );
		ServiceDescription sd = new ServiceDescription();
		sd.setType( "scheduling" );
		sd.setName( getLocalName() + "-scheduling" );
		dfd.addServices( sd );
		try {
			DFService.register( this, dfd );
		}
		catch ( FIPAException fe ) {
			fe.printStackTrace();
		}
		
		// Query agents from AMS
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
		
		// Construct query for joblist
		jobQuery = newMsg( ACLMessage.QUERY_REF );
		jobQuery.setConversationId(genCID());
		jobQuery.setContent("joblist?");
		for ( AMSAgentDescription agent: agents ) {
			jobQuery.addReceiver( agent.getName() );
		}
		
		// Add behaviours
		addBehaviour( new SubscriptionServer() );
		addBehaviour( new ReceiveJobListQuery() );
		addBehaviour( new GetJobList() );
		addBehaviour( new PublishSchedule( this,500 ) );
	}
	
	/*
	 * Behaviour: 	sends a joblist query until it is received successfully
	 * 
	 */
	private class GetJobList extends SimpleBehaviour {
		
		private static final long serialVersionUID = -2934028333565532421L;

		@Override
		public void action() {
			send ( jobQuery );
		}

		@Override
		public boolean done() {
			if ( joblistReceived )
				return true;
			return false;
		}	
	}
	
	/*
	 * Behaviour: 	publishes schedule to all subscribers
	 *
	 */
	private class PublishSchedule extends TickerBehaviour {
		
		private static final long serialVersionUID = -3832723324728838102L;
		
		public PublishSchedule( Agent a, long period ) {
			super( a, period );
		}
		
		@Override
		public void onTick() {
			if ( schedule.size() == subscribers.size() ) {
	    		for ( int agent = 0; agent < subscribers.size(); agent++ ) {
					ACLMessage scheduleMessage = newMsg( ACLMessage.PROPAGATE );
					try {
						scheduleMessage.setContentObject( schedule.get(agent) );
					} catch ( IOException e ) {
						e.printStackTrace();
					}
					scheduleMessage.addReceiver( subscribers.get(agent) );
					send( scheduleMessage );
			    }
			}
			block();
		}
	}
	
	/*
	 * Behaviour: 	receives joblist and calls method calculating schedule
	 * 
	 */
	private class ReceiveJobListQuery extends SimpleBehaviour {
		
		private static final long serialVersionUID = -3832723334788838104L;
		private MessageTemplate templateJobList = MessageTemplate.and( MessageTemplate.MatchPerformative( ACLMessage.INFORM ),
																	   MessageTemplate.MatchConversationId(jobQuery.getConversationId()) );
		
		@Override
		@SuppressWarnings("unchecked")
		public void action() {
			ACLMessage msg = receive( templateJobList );
			if ( !joblistReceived && msg != null ) {
				try {
					joblist = ( ArrayList<Job> ) msg.getContentObject();
					schedule = calculateSchedule( joblist );
					joblistReceived = true;
				} catch ( UnreadableException e ) {
					e.printStackTrace();
				}
			}
		}

		@Override
		public boolean done() {
			if ( joblistReceived )
				return true;
			return false;
		}
	}
	
	/*
	 * Behaviour: 	services subscription requests
	 * 				Always active
	 * 
	 */
	private class SubscriptionServer extends CyclicBehaviour {
		
		private static final long serialVersionUID = -3832723334788838104L;
		private MessageTemplate templateSubscriptionQuery = MessageTemplate.and( MessageTemplate.MatchPerformative( ACLMessage.SUBSCRIBE ),
																	   			 MessageTemplate.MatchContent( "scheduler" ) );

		@Override
		public void action() {
			ACLMessage msg = receive( templateSubscriptionQuery );
			if ( msg != null ) {
				try {
					if ( !subscribers.contains(msg.getSender()) ) {
						subscribers.add( msg.getSender() );
						schedule = calculateSchedule( joblist );
						System.out.println( "Added subscriber to schedule: " + msg.getSender() + " and recalculated schedule" );
					}
				} catch ( Exception e ){
					System.out.println( e );
				}
			}
			block();
		}
	}
	
	/* 
	 * Simple scheduling algorithm, near-optimal but non-flexible for n subscribers
	 * Picks every n-th job, leading to close-to-optimal schedules
	 * Non-flexible, because we do not know how to react to e.g. interference in 
	 * job executors (no feedback).
	 */
	protected ArrayList<ArrayList<Job>> calculateSchedule(ArrayList<Job> joblist_) {
		
		ArrayList<ArrayList<Job>> schedule_ = new ArrayList<ArrayList<Job>>();
		Collections.sort( joblist_ );
		
	    if ( joblist.size() > 0 && subscribers.size() > 0 ) {
	    	for ( int i = 0; i < subscribers.size(); i++ ) {
	    		ArrayList<Job> singleMachineSchedule = new ArrayList<Job>();
	    		for ( int j = 0; i+j < joblist_.size(); j = j + subscribers.size() )
	    			singleMachineSchedule.add( joblist_.get(i+j) );
	    		
	    		schedule_.add( singleMachineSchedule );
	    	}
	    }
	    return schedule_;
	}
	
	@Override
	protected void takeDown() {
		
		System.out.println( "SchedulingVisualizer " + getAID().getName() + " terminating." );
		try {
			DFService.deregister( this );
		}
			catch ( FIPAException fe ) {
			fe.printStackTrace();
		}
	}
	
	protected String genCID() {
		
		if ( cidBase==null ) {
			cidBase = getLocalName() + hashCode() +
                      System.currentTimeMillis()%10000 + "_";
		}
		return cidBase + ( cidCnt++ ); 
	}
	
	protected ACLMessage getMessage() { return jobQuery; }
	
	ACLMessage newMsg( int perf, String content, AID dest) {
		ACLMessage msg = newMsg( perf );
		if ( dest != null ) msg.addReceiver( dest );
			msg.setContent( content );
		return msg;
	}

	ACLMessage newMsg( int perf ) {
		ACLMessage msg = new ACLMessage( perf );
		msg.setConversationId( genCID() );
		return msg;
	}
}