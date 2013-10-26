import java.util.ArrayList;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

import jade.core.Agent;
import jade.core.AID;
import jade.util.leap.Iterator;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.ACLMessage;

public class JobSupplierAgent extends Agent{
	
	private static final long serialVersionUID = -6918861190459111898L;
	String filename = "";
	ArrayList<Job> joblist = new ArrayList<Job>();
	public MessageTemplate template;
	private ACLMessage msg, reply;
	protected static int cidCnt = 0;
	String cidBase;
	
	protected void setup() {
		System.out.println("JobSupplierAgent "+ getAID().getName()+" is ready.");
		Object[] args = getArguments();
		
		//if (args != null && args.length > 0) {
		//	filename = (String) args[0];
		    filename = "src/multiagent-systems-brsu/jobs";
			System.out.println("FILENAME" + filename);
			try {
				readJobs();
			} catch (IOException e) {
				e.printStackTrace();
			}
			printJobs();
		//}
		
	    template = MessageTemplate.MatchPerformative( ACLMessage.QUERY_REF ); 
		
		addBehaviour(new CyclicBehaviour(this)
	      {

			private static final long serialVersionUID = 8693491533514569273L;

			public void action()  
	         {
	            ACLMessage msg = receive( template );
	            if (msg!=null && msg.getContent().equals("joblist")) {
	                reply = msg.createReply();
	                reply.setPerformative( ACLMessage.INFORM );
	                try {
						reply.setContentObject(joblist);
					} catch (IOException e) {
						e.printStackTrace();
					}
	                send(reply);
	            }
	            block();
	         }
	      });		
		
	}
	
	protected void takeDown() {
		System.out.println("JobSupplierAgent "+getAID().getName()+" terminating.");
	}
	
	protected void readJobs() throws IOException {
		FileReader fileReader = new FileReader(filename);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String line;
		String[] columnDetail;
		while ((line = bufferedReader.readLine()) != null) {
			columnDetail = line.split(" ");
			Job newJob = new Job();
			newJob.setName(columnDetail[0]);
			newJob.setDuration(Integer.parseInt(columnDetail[1]));
			joblist.add(newJob);
		}
		bufferedReader.close();
		fileReader.close();
		return;
	}
	
	protected void printJobs() {
		for(int i = 0; i < joblist.size(); i++) {
			System.out.println(joblist.get(i).getName() + " - " + joblist.get(i).getDuration() + "\n");
		}		
	}
	
	protected ACLMessage getMessage() { return msg; }
	
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