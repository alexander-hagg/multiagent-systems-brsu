package multiagent_scheduler;

import java.util.ArrayList;

import jade.core.AID;
import jade.util.leap.Serializable;

public class Schedule implements Serializable {

	private static final long serialVersionUID = 4630566579557849268L;
	public AID owner;
	public ArrayList<Job> schedule;
	private int scheduleStartTime, scheduleEndTime;
	private ArrayList<Integer> jobStartTimes;
	private ArrayList<Integer> jobDueTimes;
	
	public Schedule() {
		schedule = new ArrayList<Job>();
		jobStartTimes = new ArrayList<Integer>();
		jobDueTimes = new ArrayList<Integer>();
	}

	public int getScheduleStartTime() {
		return scheduleStartTime;
	}

	public void setScheduleStartTime(int scheduleStartTime) {
		this.scheduleStartTime = scheduleStartTime;
		jobStartTimes.clear();
		jobDueTimes.clear();
		int timePointer = this.scheduleStartTime;
		for ( Job job : schedule ) {
			jobStartTimes.add( timePointer );
			timePointer +=  job.getProcessingTime();
			jobDueTimes.add( timePointer );
		}
		scheduleEndTime = timePointer;
	}

	public int getScheduleEndTime() {
		return scheduleEndTime;
	}

	public ArrayList<Integer> getJobStartTimes() {
		return jobStartTimes;
	}

	public ArrayList<Integer> getJobDueTimes() {
		return jobDueTimes;
	}
	
}
