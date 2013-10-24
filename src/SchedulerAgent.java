import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.ACLMessage;

public class SchedulerAgent extends Agent {

	private static final long serialVersionUID = -6918861190459111898L;
	protected static int cidCnt = 0;
	String cidBase;
	private ACLMessage msg;
	private MessageTemplate template;

	
	
	protected void setup() {
		System.out.println("SchedulingVisualizer "+ getAID().getName() + " is ready.");
		
		msg = newMsg( 	ACLMessage.QUERY_REF, "",
                		new AID( "jobSupplier1", AID.ISLOCALNAME) );
		
		template = MessageTemplate.and( MessageTemplate.MatchPerformative( ACLMessage.INFORM ),
	            						MessageTemplate.MatchConversationId( msg.getConversationId() ));
		
		addBehaviour( new myReceiver(this, 1000, template )
        {
			private static final long serialVersionUID = 8693491577914569273L;

			public void handle( ACLMessage msg ) 
			{  
				if (msg == null) 
					System.out.println("SchedulerAgent: Timeout");
				else 
					System.out.println("SchedulerAgent received jobs: $"+ msg);
  
           }
        });
      
		send ( msg );
		
	}
	
	protected void takeDown() {
	
		System.out.println("SchedulingVisualizer "+getAID().getName()+" terminating.");
	}
	
	// helper methods
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