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

public class SchedulingVisualizer extends Agent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6918861190459111898L;
	protected static int cidCnt = 0;
	String cidBase;
	private ACLMessage msg;
	private MessageTemplate template, templateSchedule;
	AMSAgentDescription [] agents = null;
	ArrayList<Integer> schedule = new ArrayList<Integer>();
	SchedulingVisualizerGui visGui =  new SchedulingVisualizerGui();
	
	protected void setup() {
		System.out.println("SchedulingVisualizer "+getAID().getName()+" is ready.");
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
		}) ;
		
		templateSchedule = MessageTemplate.MatchPerformative( ACLMessage.INFORM ); 
		
	
		addBehaviour( new CyclicBehaviour(this)
	      {
			private static final long serialVersionUID = -8703203027122857880L;

			public void action()  
	         {
	            ACLMessage msg = receive( templateSchedule );
	            if (msg == null) 
					System.out.println("SchedulerAgentVisualizer: Timeout");
				else  {
					System.out.println("SchedulerAgentVisualizer received schedule \n=======================");
					try {
						schedule = (ArrayList<Integer>) msg.getContentObject();
						System.out.println("SCHEDULE:\n");
						int totalTime = 0;
						for (int job : schedule) {
							System.out.println(job + "\n");
							totalTime += job;
						}
						//DISPLAY GUI
						visGui.showGui(schedule, totalTime);
					} catch (UnreadableException e) {
						e.printStackTrace();
					}
				}
	            block();
	         }
			
		});
		
	}
	
	protected void takeDown() {
	
		System.out.println("SchedulingVisualizer "+getAID().getName()+" terminating.");
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