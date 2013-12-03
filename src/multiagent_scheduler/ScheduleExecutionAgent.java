package multiagent_scheduler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.HashSet;
import java.util.Set;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.SubscriptionResponder;
import jade.proto.SubscriptionResponder.Subscription;
import jade.proto.SubscriptionResponder.SubscriptionManager;

public class ScheduleExecutionAgent extends Agent{

	private static final long serialVersionUID = 4853132690502168830L;
	SystemTime ticker;
	private Vector<AID> schedulerAgents = new Vector<AID>();
	ACLMessage subscribeQuery;
	private String cidBase;
	protected static int cidCnt = 0;
	boolean subscribed = false;
	boolean waitingForSubscription = false;
	Schedule schedule = new Schedule();
	private Set<Subscription> subscriptions = new HashSet<Subscription>();
	SubscriptionManager sm;

	protected void setup() {
		System.out.println("ScheduleExecutionAgent "+ getAID().getName() + " is ready.");
		ticker = new SystemTime(this);
		addBehaviour(ticker);
		
		// register executor service
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sdExecutor = new ServiceDescription();
		sdExecutor.setType("executing");
		sdExecutor.setName(getLocalName()+"-executing");
		dfd.addServices(sdExecutor);
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
		// find scheduler service
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType("scheduling");
		template.addServices(sd);
		try {
			DFAgentDescription[] result = DFService.search( this,
															template);
			schedulerAgents.clear();
			for (int i = 0; i < result.length; ++i) {
				schedulerAgents.addElement(result[i].getName());
			}
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
		addBehaviour( new SubscribeQuery() );
		addBehaviour( new ReceiveSchedule() );
		
		
		// FIPA SUBPUBSTUFF
        sm = new SubscriptionManager() {
        	 
            public boolean register(Subscription subscription) {
                subscriptions.add(subscription);
                return true;
            }
 
            public boolean deregister(Subscription subscription) {
            	subscriptions.remove(subscription);
                return true;
            }
        };
		MessageTemplate templateScheduleSubscription = MessageTemplate.and( MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
				MessageTemplate.MatchContent("send-schedules"));
		
		addBehaviour( new ScheduleResponder(this, templateScheduleSubscription, sm) );
		addBehaviour( new ScheduleResponse(this, 200) );
	}
	
    private class ScheduleResponse extends TickerBehaviour {
		private static final long serialVersionUID = 7170301332202656112L;
		ScheduleExecutionAgent seaAgent;
		public ScheduleResponse(Agent agent, long time) {
            super(agent, time);
            seaAgent = (ScheduleExecutionAgent) agent;
        }
 
        public void onTick() {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            // TODO do not do this here! check for jobs done
            
            int count = 0;
            for ( int jobDueTime: schedule.getJobDueTimes() ) {
            	if (jobDueTime < seaAgent.ticker.systemTime) {
            		schedule.jobsDone.set( count, true );
            	}
            	count++;
            }
            
            try {
				msg.setContentObject( schedule );
			} catch (IOException e) {
				e.printStackTrace();
			}
            for ( Subscription subscription: ((ScheduleExecutionAgent)myAgent).subscriptions ) {
            	subscription.notify(msg);
            }
        }
    }
	
	protected void takeDown() {
		System.out.println("ScheduleExecutionAgent  " + getAID().getName() + " terminating.");
	}
	
    private class ScheduleResponder extends SubscriptionResponder {
		private static final long serialVersionUID = 5568721132409065755L;
		private Subscription subscription;
 
        public ScheduleResponder(Agent agent, MessageTemplate mt, SubscriptionManager sm) {
            super(agent, mt, sm);
        }
 
        protected ACLMessage handleSubscription(ACLMessage proposal)
                throws NotUnderstoodException {
            // System.out.printf("%s: SUSCRIBE received from %s.\n", getLocalName(), proposal.getSender().getLocalName());
            // System.out.printf("%s: Proposal contains: %s.\n", getLocalName(), proposal.getContent());
 
            if (checkProposal(proposal.getContent())) {
 
                this.subscription = this.createSubscription(proposal);
 
                try {
                    this.mySubscriptionManager.register(subscription);
                } catch (Exception e) {
                    System.out.println(getLocalName() + ": Error registering the subscriber.");
                }
                ACLMessage agree = proposal.createReply();
                agree.setPerformative(ACLMessage.AGREE);
                return agree;
            } else {
                ACLMessage refuse = proposal.createReply();
                refuse.setPerformative(ACLMessage.REFUSE);
                return refuse;
            }
        }
 
        private boolean checkProposal(String content) {
        	if ( content.compareTo( "send-schedules" ) == 0 ) {
        		return true;
        	}
			return false;
		}

		protected ACLMessage handleCancel(ACLMessage cancelation) {
            //System.out.printf("%s: CANCEL reveiced from %s.\n", getLocalName(), cancelation.getSender().getLocalName());
 
            try {
                this.mySubscriptionManager.deregister(this.subscription);
            } catch (Exception e) {
                System.out.println(getLocalName() + ": Error deregistering subscriber.");
            }
 
            ACLMessage cancel = cancelation.createReply();
            cancel.setPerformative(ACLMessage.INFORM);
            return cancel;
        }
    }
	
	private class SubscribeQuery extends SimpleBehaviour {
		
		private static final long serialVersionUID = -3832723334788838104L;
		private MessageTemplate templateSubscriptionSuccess;
		
		@Override
		public boolean done() {
			if (subscribed)
				return true;
			return false;
		}
		
		@Override
		public void action() {
			// subscribe to scheduler service
			if (!waitingForSubscription && !subscribed) {
				subscribeQuery = newMsg( ACLMessage.SUBSCRIBE );
				subscribeQuery.setContent("scheduler");
				for ( AID agent: schedulerAgents ) {
					subscribeQuery.addReceiver( agent );
				}
				templateSubscriptionSuccess = MessageTemplate.and( MessageTemplate.MatchPerformative( ACLMessage.CONFIRM ),
						   MessageTemplate.MatchConversationId(subscribeQuery.getConversationId()) );
				send ( subscribeQuery );
				waitingForSubscription = true;
			} else if (!subscribed) {
				ACLMessage msg = receive( templateSubscriptionSuccess );
				if (msg != null) {
					subscribed = true;
				}
			}			
			block();
		}
		 
		
		
	}
	
	private class ReceiveSchedule extends CyclicBehaviour {
		
		private static final long serialVersionUID = -3832727338788838104L;
		private MessageTemplate templateSubscriptionSuccess;
		@SuppressWarnings("unchecked")
		public void action() {
			
			for ( AID agent: schedulerAgents ) {
				templateSubscriptionSuccess = MessageTemplate.and( MessageTemplate.MatchPerformative( ACLMessage.PROPAGATE ),
																   MessageTemplate.MatchSender(agent) );
				ACLMessage msg = receive( templateSubscriptionSuccess );
				if (msg != null) {
					try {
						schedule = (Schedule) msg.getContentObject();
						// System.out.println("=================\nSCHEDULE RECEIVED" + getAID());
						// print(schedule);
						// System.out.println("\n=================");
						
					} catch (UnreadableException e) {
						e.printStackTrace();
					}
				}
			}
			block();
		}
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
	
	protected void print(ArrayList<Job> joblist) {
		System.out.println("job schedule:\njob name\t\tduration\n=========================================");
		for(int i = 0; i < joblist.size(); i++) {
			System.out.println(joblist.get(i).getJobNumber() + "\t\t\t" + joblist.get(i).getProcessingTime() + " hours");
		}
		System.out.println("=========================================");
	}
}
