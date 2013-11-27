package multiagent_scheduler;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.AMSService;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.states.MsgReceiver;
import jade.proto.SubscriptionInitiator;

public class SchedulingVisualizerAgent extends Agent{

	private static final long serialVersionUID = -6918861190459111898L;
	protected static int cidCnt = 0;
	String cidBase;
	// private MessageTemplate template, templateSchedule;
	private Vector<AID> executorAgents = new Vector<AID>();
	SchedulingVisualizerGui visGui =  new SchedulingVisualizerGui();
	// private boolean guiSetup = false;
	SystemTime ticker;
	
	protected void setup() {
		System.out.println("SchedulingVisualizer " + getAID().getName() + " is ready.");
		
		ticker = new SystemTime(this);
		addBehaviour( ticker );
		addBehaviour( new FindJobExecutors(this, 1000) );
		//addBehaviour( new GetSchedules(this, 1000) );
		//addBehaviour( new ReceiveSchedules() );
		
		// Wait for system to setup before starting subscription
		addBehaviour( new WakerBehaviour(this, 2000) {
			private static final long serialVersionUID = 5104515388242735848L;
			@Override
			protected void onWake() {
				super.onWake();
				ACLMessage msgSubscribeScheduleExecutors = new ACLMessage(ACLMessage.CFP);
		  		for (AID agent : executorAgents) {
		  			msgSubscribeScheduleExecutors.addReceiver( agent);
		  		}
		  		msgSubscribeScheduleExecutors.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
				// We want to receive a reply within 10 secs
		  		msgSubscribeScheduleExecutors.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
		  		msgSubscribeScheduleExecutors.setContent("send-schedules");
				
				myAgent.addBehaviour( new SchedulingVisualizerAgent.ScheduleSubscription(myAgent, msgSubscribeScheduleExecutors) );
			}
		});
  		
				
	}
	
	private class ScheduleSubscription extends SubscriptionInitiator {

		private static final long serialVersionUID = 4331598324021328671L;

		public ScheduleSubscription(Agent a, ACLMessage msg) {
			super(a, msg);
		}
		
		protected void handleAgree(ACLMessage agree) {
			System.out.println("SVA received subscription AGREE from " + agree.getSender());
		}
		
		protected void handleInform(ACLMessage inform) {
			System.out.println("SVA received subscription INFORM from " + inform.getSender());
			System.out.println(inform.getContent());
		}
		
		
	}

/*	
	private class GetSchedules2 extends TickerBehaviour {
		private static final long serialVersionUID = -2248363625608297714L;

		public GetSchedules(Agent a, long period) {
			super(a, period);
		}

		@Override
		protected void onTick() {
			for (AID agent : executorAgents) {
				ACLMessage msgScheduleRequest = newMsg( ACLMessage.QUERY_REF );
				msgScheduleRequest.setContent("q: schedule for visualization");
				msgScheduleRequest.setConversationId( genCID() );
				msgScheduleRequest.set
				msgScheduleRequest.addReceiver( agent );
				send ( msgScheduleRequest );
			}
		}
	}
	
	private class ReceiveSchedules extends CyclicBehaviour {

		private static final long serialVersionUID = -2246435472350875607L;
		private MessageTemplate templateJobList = MessageTemplate.and( MessageTemplate.MatchPerformative( ACLMessage.INFORM ),
																	   MessageTemplate.MatchProtocol());

		@SuppressWarnings("unchecked")
		public void action() {
			ArrayList<Job> schedule = new ArrayList<Job>();
			ACLMessage msgScheduleReply = receive( templateJobList );
			System.out.println("Received schedule of agent " + msgScheduleReply.getSender());
			if (msgScheduleReply != null) {
				try {
					schedule = (ArrayList<Job>) msgScheduleReply.getContentObject();
				} catch ( UnreadableException e ) {
					e.printStackTrace();
				}
			}
		}
	}
	
	else if( msg.getConversationId().substring(0,3).compareTo("Sch")==0  ) {
		try {
			schedule = (ArrayList<Job>) msg.getContentObject();
			//print(schedule);
			int totalTime = 0;
			for (Job job : schedule) {
				totalTime += job.getProcessingTime();
			}
			
			//DISPLAY GUI
			if (!guiSetup) {
				visGui.showGui(schedule, totalTime);
				guiSetup = true;
			} else {
				visGui.refreshGui(schedule, totalTime);
			}
			
		} catch (UnreadableException e) {
			e.printStackTrace();
		}
	
	*/
	
	private class FindJobExecutors extends TickerBehaviour {
		
		private static final long serialVersionUID = -3832753334788838104L;
		private FindJobExecutors(Agent a, long period) {
			super(a, period);
		}

		@Override
		protected void onTick() {
			// find executor services
			DFAgentDescription agentDescriptionTemplate = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType("executing");
			agentDescriptionTemplate.addServices(sd);
			try {
				DFAgentDescription[] result = DFService.search( myAgent,
																agentDescriptionTemplate);
				for (int i = 0; i < result.length; ++i) {
					if (!executorAgents.contains(result[i].getName())) {
						System.out.println("Found new executor agent: " + result[i].getName());
						executorAgents.addElement(result[i].getName());
					}
				}
			}
			catch (FIPAException fe) {
				fe.printStackTrace();
			}
		}
	}
	
	protected void takeDown() {
		System.out.println("SchedulingVisualizer " + getAID().getName() + " terminating.");
	}
	
	protected void print(ArrayList<Job> joblist) {
		System.out.println("job schedule:\njob name\t\tduration\n=========================================");
		for(int i = 0; i < joblist.size(); i++) {
			System.out.println(joblist.get(i).getJobNumber() + "\t\t\t" + joblist.get(i).getProcessingTime() + " hours");
		}
		System.out.println("=========================================");
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