package multiagent_scheduler;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.*;

public class SystemTime extends CyclicBehaviour
{ 
	private static final long serialVersionUID = 4831397421211155970L;
	private ACLMessage msg;
	private MessageTemplate templateSchedule;
	private Agent agent;
	public int systemTime;
	
	public SystemTime(Agent a) {
		super(a);
		agent = a;
		systemTime = 0;
		templateSchedule = MessageTemplate.and( MessageTemplate.MatchPerformative( ACLMessage.INFORM ), 
												MessageTemplate.MatchConversationId( "SYSTEMTIME" ) );
												
	}
 
	public void action()  
    {
       msg = agent.receive( templateSchedule );
       if (msg!=null) { 
    	   try {
    		   systemTime = (Integer) msg.getContentObject();
    	   } catch (UnreadableException e) {
    		   e.printStackTrace();
    	   }
       }
       block();
    }
}
