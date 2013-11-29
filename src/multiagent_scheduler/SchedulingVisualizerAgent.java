package multiagent_scheduler;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.SubscriptionInitiator;

public class SchedulingVisualizerAgent extends Agent{

	private static final long serialVersionUID = -6918861190459111898L;
	protected static int cidCnt = 0;
	String cidBase;
	private Vector<AID> executorAgents = new Vector<AID>();
	HashMap< AID,SchedulingVisualizerGui > nVisGui = new HashMap< AID,SchedulingVisualizerGui >();
	private boolean guiSetup = false;
	SystemTime ticker;
	
	protected void setup() {
		System.out.println("SchedulingVisualizer " + getAID().getName() + " is ready.");
		
		ticker = new SystemTime(this);
		addBehaviour( ticker );
		addBehaviour( new FindJobExecutors(this, 1000) );

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
			Schedule schedule;
			try {
				schedule = (Schedule) inform.getContentObject();
				//DISPLAY GUI
				if (!guiSetup) {
					nVisGui.get( inform.getSender() ).showGui(schedule.schedule, schedule.getScheduleEndTime() - schedule.getScheduleStartTime() );
					guiSetup = true;
				} else {
					nVisGui.get( inform.getSender() ).showGui(schedule.schedule, schedule.getScheduleEndTime() - schedule.getScheduleStartTime() );
					
				}
				
			} catch (UnreadableException e) {
				e.printStackTrace();
			}
		}
		
		
	}
	
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
						
						nVisGui.put( result[i].getName(),new SchedulingVisualizerGui() );
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