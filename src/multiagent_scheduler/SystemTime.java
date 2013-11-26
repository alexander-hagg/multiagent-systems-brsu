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
		templateSchedule = MessageTemplate.MatchPerformative( ACLMessage.INFORM ); 
	}
 
	public void action()  
    {
       msg = agent.receive( templateSchedule );
       if (msg!=null && msg.getContent().equals("tick")) { 
           systemTime++;
           //System.out.println("Tick " + systemTime);
       }
    }
}
