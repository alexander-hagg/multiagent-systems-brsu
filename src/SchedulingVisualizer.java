import jade.core.Agent;
import jade.core.AID;

public class SchedulingVisualizer extends Agent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6918861190459111898L;
	
	
	//private AID[] sellerAgents = {new AID("seller1", AID.ISLOCALNAME),
	
	protected void setup() {
		System.out.println("SchedulingVisualizer "+getAID().getName()+" is ready.");
		Object[] args = getArguments();
		if (args != null && args.length > 0) {
		}
	}
	
	protected void takeDown() {
	
		System.out.println("SchedulingVisualizer "+getAID().getName()+" terminating.");
	}

}