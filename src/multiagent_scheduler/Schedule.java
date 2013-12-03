package multiagent_scheduler;

import java.util.ArrayList;

import jade.core.AID;
import jade.util.leap.Serializable;

public class Schedule implements Serializable {

	private static final long serialVersionUID = 4630566579557849268L;
	public AID owner;
	private ArrayList<Job> schedule;
	private int scheduleStartTime = -1;
	private int scheduleEndTime = -1;
	private ArrayList<Integer> jobStartTimes;
	private ArrayList<Integer> jobDueTimes;
	public ArrayList<Boolean> jobsDone;
	
	public Schedule() {
		schedule = new ArrayList<Job>();
		jobStartTimes = new ArrayList<Integer>();
		jobDueTimes = new ArrayList<Integer>();
		jobsDone = new ArrayList<Boolean>();
	}
	
	public void set(int scheduleStartTime) {
		this.scheduleStartTime = scheduleStartTime;
		int timePointer = this.scheduleStartTime;
		for ( int jobNr = 0; jobNr < schedule.size(); jobNr++ ) {
			jobStartTimes.set( jobNr, timePointer );
			timePointer +=  schedule.get(jobNr).getProcessingTime();
			jobDueTimes.set( jobNr, timePointer );
		}
		scheduleEndTime = timePointer;
	}
	
	public boolean add( Job job ) {
		if ( this.scheduleStartTime < 0 ) {
			System.out.println( "Schedule has not been initiated. Please provide an "
					+ "int scheduleStartTime as well" );
			return false;
		}
		int timePointer;
		if ( schedule.size() == 0 ) {
			timePointer = this.scheduleStartTime;
		} else {
			timePointer = this.scheduleEndTime;
		}
		jobStartTimes.add( timePointer );
		timePointer +=  job.getProcessingTime();
		jobDueTimes.add( timePointer );
		jobsDone.add( false );
		schedule.add( job );
		this.scheduleEndTime = timePointer;
		return true;
	}
	
	public boolean add( Job job, int scheduleStartTime ) {
		if ( this.scheduleStartTime < 0 ) {
			this.scheduleStartTime = scheduleStartTime;
			return add( job );
		} else {
			System.out.println( "Failed trying to add a job and a schedule start time "
					+ "to a schedule that has already been initialized with a start time.");
			return false;
		}
	}

	public ArrayList<Job> getSchedule() {
		return schedule;
	}

	public int getScheduleStartTime() {
		return scheduleStartTime;
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
