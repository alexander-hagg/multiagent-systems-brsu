package multiagent_scheduler;

import java.util.ArrayList;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
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
	AMSAgentDescription [] agents = null;
	ArrayList<Job> schedule = new ArrayList<Job>();
	SchedulingVisualizerGui visGui =  new SchedulingVisualizerGui();
	private boolean guiSetup = false;
	SystemTime ticker;
	private Object stateLock1;
	
	protected void setup() {
		System.out.println("SchedulingVisualizer " + getAID().getName() + " is ready.");
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

		addBehaviour( new TickerBehaviour( this, 1000 )
		{
			private static final long serialVersionUID = -7184254332416821810L;
			
			protected void onTick() {
				for (AMSAgentDescription agent: agents) {
					msg.addReceiver( agent.getName() );
				}
				send ( msg );
			}
		});
		
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
					System.out.println(msg.getConversationId().substring(0,3));
					try {
						schedule = (ArrayList<Job>) msg.getContentObject();
						print(schedule);
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
		addBehaviour(ticker);
		
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