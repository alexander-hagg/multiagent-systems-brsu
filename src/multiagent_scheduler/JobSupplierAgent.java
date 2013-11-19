package multiagent_scheduler;

import java.util.ArrayList;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.text.ParseException;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.ACLMessage;

public class JobSupplierAgent extends Agent{
	
	private static final long serialVersionUID = -6918861190459111898L;
	String filename = "";
	ArrayList<Job> joblist;
	public MessageTemplate template;
	private ACLMessage msg, reply;
	protected static int cidCnt = 0;
	String cidBase;
	FileReader fileReader;
	BufferedReader bufferedReader;
	
	protected void setup() {
		System.out.println("JobSupplierAgent "+ getAID().getName()+" is ready.");
		Object[] args = getArguments();
		
		if (args != null && args.length > 0) {
			filename = (String) args[0];
			try {
				joblist = read(filename);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
			print(joblist);
		}
		
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
						try {
							bufferedReader.close();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						try {
							fileReader.close();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
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
	
	protected ArrayList<Job> read(String filename) throws IOException, ParseException{
		ArrayList<Job> joblist = new ArrayList<Job>();
		fileReader = new FileReader(filename);
		bufferedReader = new BufferedReader(fileReader);
		String line;
		String[] columnDetail;
		int lineCounter = 0;
		while ((line = bufferedReader.readLine()) != null) {
			lineCounter++;
			columnDetail = line.split("; ");
			Job newJob = new Job();
			newJob.setJobNumber(Integer.parseInt(columnDetail[0]));
			
			if (Integer.parseInt(columnDetail[1]) > 0) {
				newJob.setProcessingTime(Integer.parseInt(columnDetail[1]));
			}
			else {
				bufferedReader.close();
				fileReader.close();
				throw new ParseException("processing time should be an integer greater than zero", lineCounter);
			}
			
			if (Integer.parseInt(columnDetail[2]) > -1) {
				newJob.setReleaseTime(Integer.parseInt(columnDetail[2]));
			}
			else {
				bufferedReader.close();
				fileReader.close();
				throw new ParseException("release time should be an integer greater than or equal zero", lineCounter);
			}
			
			if (Integer.parseInt(columnDetail[3]) == 0 || Integer.parseInt(columnDetail[3]) >= (newJob.getProcessingTime()+newJob.getReleaseTime() ) ) {
				newJob.setDueDate(Integer.parseInt(columnDetail[3]));
			} else {
				bufferedReader.close();
				fileReader.close();
				throw new ParseException("due date should be an integer greater than or equal zero. When greater zero it should be greater than processing time plus release time", lineCounter);
			}
			
			if (Integer.parseInt(columnDetail[4])==0 || Integer.parseInt(columnDetail[4])==1 ) {
				newJob.setPreemptable(Integer.parseInt(columnDetail[4]));
			} else {
				bufferedReader.close();
				fileReader.close();
				throw new ParseException("set preemptable should be an integer equalling 0 or 1", lineCounter);
			}
			
			if (Integer.parseInt(columnDetail[5]) > -1) {
				newJob.setWeight(Integer.parseInt(columnDetail[5]));
			} else {
				bufferedReader.close();
				fileReader.close();
				throw new ParseException("weight should be an integer greater than or equal zero", lineCounter);
			}
			
			if (Integer.parseInt(columnDetail[6]) > -1) {
				newJob.setCost(Integer.parseInt(columnDetail[6]));
			} else {
				bufferedReader.close();
				fileReader.close();
				throw new ParseException("cost should be an integer greater than or equal zero", lineCounter);
			}
			
			if (Integer.parseInt(columnDetail[7]) > -1) {
				newJob.setProfit(Integer.parseInt(columnDetail[7]));
			} else {
				bufferedReader.close();
				fileReader.close();
				throw new ParseException("weight should be an integer greater than or equal zero", lineCounter);
			}
			
			joblist.add(newJob);
		}
		bufferedReader.close();
		fileReader.close();
		return joblist;
	}
	
	protected void print(ArrayList<Job> joblist) {
		System.out.println("list of jobs:\njob name\t\tduration\n=========================================");
		for(int i = 0; i < joblist.size(); i++) {
			System.out.println(joblist.get(i).getJobNumber() + "\t\t\t" + joblist.get(i).getProcessingTime() + " hours");
		}
		System.out.println("=========================================");
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