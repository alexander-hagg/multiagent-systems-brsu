package multiagent_scheduler;

import jade.util.leap.Serializable;

public class Job implements Serializable, java.lang.Comparable<Job> {

	private static final long serialVersionUID = 4630916579557849268L;
	private int jobNumber = 0;
	private int processingTime = 0;
	private int releaseTime = 0;
	private int dueDate = 0;
	private int preemptable = 0;
	private int weight = 0;
	private int cost = 0;
	private int profit = 0;

	public int getJobNumber() {
		return jobNumber;
	}

	public void setJobNumber(int jobNumber) {
		this.jobNumber = jobNumber;
	}

	public int getProcessingTime() {
		return processingTime;
	}

	public void setProcessingTime(int processingTime) {
		this.processingTime = processingTime;
	}

	public int getReleaseTime() {
		return releaseTime;
	}

	public void setReleaseTime(int releaseTime) {
		this.releaseTime = releaseTime;
	}

	public int getDueDate() {
		return dueDate;
	}

	public void setDueDate(int dueDate) {
		this.dueDate = dueDate;
	}

	public int getPreemptable() {
		return preemptable;
	}

	public void setPreemptable(int preemptable) {
		this.preemptable = preemptable;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public int getCost() {
		return cost;
	}

	public void setCost(int cost) {
		this.cost = cost;
	}

	public int getProfit() {
		return profit;
	}

	public void setProfit(int profit) {
		this.profit = profit;
	}
	
	public int compareTo(Job o) {
		if (this.processingTime > o.processingTime)
			return 1;
		else if (this.processingTime < o.processingTime)
			return -1;
		else 
			return 0;
	}

}
