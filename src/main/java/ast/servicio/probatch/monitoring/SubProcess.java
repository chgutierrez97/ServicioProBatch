package ast.servicio.probatch.monitoring;

public class SubProcess {

	private String pid;
	private String user;
	private String name;
	private String state;
	private String time;

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "PID : " + getPid() + " USER : " + getUser() + " NAME : " + getName() + " STATE : " + getState() + " TIME : " + getTime();
	}

}
