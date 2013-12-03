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
	
	SystemTime ticker;
	protected static int cidCnt = 0;
	String cidBase;
	private ACLMessage jobQuery;
	private boolean joblistReceived = false;
	
	ArrayList<Job> joblist = new ArrayList<Job>();
	ArrayList<Schedule> nSchedule = new ArrayList<Schedule>();
	Vector<AID> subscribers = new Vector<AID>();
	private Vector<AID> jobSuppliers = new Vector<AID>();
	
	@Override
	protected void setup() 
	{
		System.out.println( "SchedulerAgent "+ getAID().getName() + " is ready." );
		ticker = new SystemTime(this);
		addBehaviour(ticker);
		
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
		
		// find jobsupplier service
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sdJobSupplier = new ServiceDescription();
		sdJobSupplier.setType("jobsupplying");
		template.addServices(sdJobSupplier);
		try {
			DFAgentDescription[] result = DFService.search( this,
															template);
			jobSuppliers.clear();
			for (int i = 0; i < result.length; ++i) {
				jobSuppliers.addElement(result[i].getName());
			}
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
		
		// Construct query for joblist
		jobQuery = newMsg( ACLMessage.QUERY_REF );
		jobQuery.setConversationId(genCID());
		jobQuery.setContent("joblist?");
		for ( AID agent: jobSuppliers ) {
			jobQuery.addReceiver( agent );
		}
		
		// Add behaviours
		addBehaviour( new SubscriptionServer() );
		addBehaviour( new ReceiveJobListQuery() );
		addBehaviour( new GetJobList() );
		addBehaviour( new PublishSchedule( this,2000 ) );
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
	
	// TODO implement FIPA ContractNet protocol to handle this!
	private class PublishSchedule extends TickerBehaviour {
		
		private static final long serialVersionUID = -3832723324728838102L;
		
		public PublishSchedule( Agent a, long period ) {
			super( a, period );
		}
		
		@Override
		public void onTick() {
			if ( nSchedule.size() == subscribers.size() ) {
	    		for ( int agent = 0; agent < subscribers.size(); agent++ ) {
					ACLMessage scheduleMessage = newMsg( ACLMessage.PROPAGATE );
					try {
						scheduleMessage.setContentObject( nSchedule.get(agent) );
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
					if ( subscribers.size() > 0 )
						nSchedule = calculateSchedule( joblist );
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
						nSchedule = calculateSchedule( joblist );
						System.out.println( "Added subscriber to schedule: " + msg.getSender() + " and recalculated schedule" );
						//TODO send confirmation to subscriber
						ACLMessage reply = msg.createReply();
						reply.setPerformative( ACLMessage.CONFIRM );
						send( reply );
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
	protected ArrayList<Schedule> calculateSchedule(ArrayList<Job> joblist_) {
		
		ArrayList<Schedule> nSchedule_ = new ArrayList<Schedule>();
		Collections.sort( joblist_ );
		
	    if ( joblist.size() > 0 && subscribers.size() > 0 ) {
	    	for ( int i = 0; i < subscribers.size(); i++ ) {
	    		Schedule singleMachineSchedule = new Schedule();
	    		
	    		for ( int j = 0; i+j < joblist_.size(); j += subscribers.size() ) {
	    			// add a setter to the schedule in Schedule class
	    			singleMachineSchedule.schedule.add( joblist_.get(i+j) );
	    		}
	    			
	    		singleMachineSchedule.owner = subscribers.get(i);
	    		singleMachineSchedule.setScheduleStartTime( ticker.systemTime );	
	    		nSchedule_.add( singleMachineSchedule );
	    	}
	    }
	    return nSchedule_;
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