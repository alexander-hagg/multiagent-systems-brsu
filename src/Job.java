import jade.util.leap.Serializable;


public class Job implements Serializable {

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
}
