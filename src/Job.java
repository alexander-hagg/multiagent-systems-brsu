import jade.util.leap.Serializable;


public class Job implements Serializable, java.lang.Comparable<Job> {

	private static final long serialVersionUID = 4630916579557849268L;
	String name = null;
	int duration = 0;
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}


	@Override
	public int compareTo(Job o) {
		if (this.duration > o.duration)
			return 1;
		else if (this.duration > o.duration)
			return -1;
		else 
			return 0;
	}
}
