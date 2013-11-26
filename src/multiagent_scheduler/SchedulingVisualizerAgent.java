package multiagent_scheduler;

import java.util.ArrayList;
import java.util.Vector;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.AMSService;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.states.MsgReceiver;

public class SchedulingVisualizerAgent extends Agent{

	private static final long serialVersionUID = -6918861190459111898L;
	protected static int cidCnt = 0;
	String cidBase;
	private ACLMessage msg;
	private MessageTemplate template, templateSchedule;
	private Vector<AID> executorAgents = new Vector<AID>();
	ArrayList<Job> schedule = new ArrayList<Job>();
	SchedulingVisualizerGui visGui =  new SchedulingVisualizerGui();
	private boolean guiSetup = false;
	SystemTime ticker;
	private Object stateLock1;
	
	protected void setup() {
		System.out.println("SchedulingVisualizer " + getAID().getName() + " is ready.");
		
		templateSchedule = MessageTemplate.MatchPerformative( ACLMessage.INFORM );
		
		addBehaviour ( new MsgReceiver(this, templateSchedule, -1, null, stateLock1)
		{
			private static final long serialVersionUID = 8693491577914569113L;
			@SuppressWarnings("unchecked")
			public void handleMessage( ACLMessage msg )
			{
				
				if (msg == null) {
					System.out.println("SchedulingVisualizer: Timeout");
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
				} 
			}
		});
		
		ticker = new SystemTime(this);
		addBehaviour( ticker );
		
		addBehaviour( new FindJobExecutors(this, 1000) );
		addBehaviour( new GetSchedules(this, 1000) );
		addBehaviour( new ReceiveSchedules() );
		
	}
	
	private class ReceiveSchedules extends CyclicBehaviour {

		private static final long serialVersionUID = -2246435472350875607L;
		private MessageTemplate templateJobList = MessageTemplate.and( MessageTemplate.MatchPerformative( ACLMessage.INFORM ),
																	   MessageTemplate.MatchConversationId(jobQuery.getConversationId()) );

		@SuppressWarnings("unchecked")
		public void action() {
			ACLMessage msg = receive( templateJobList );
			if (!joblistReceived && msg != null) {
				try {
					joblist = (ArrayList<Job>) msg.getContentObject();
					joblistReceived = true;
					schedule = calculateSchedule( joblist );
				} catch ( UnreadableException e ) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	private class GetSchedules extends TickerBehaviour {
		private static final long serialVersionUID = -2248363625608297714L;

		public GetSchedules(Agent a, long period) {
			super(a, period);
		}

		@Override
		protected void onTick() {
			for (int agent = 0; agent < executorAgents.size(); agent++) {
				msg = newMsg( ACLMessage.QUERY_REF );
				msg.setContent("q: schedule for visualization");
				msg.setConversationId( "agent" + agent + genCID() );
				msg.addReceiver( executorAgents.get(agent) );
				send ( msg );
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