package HSTS_Entities;

import java.io.Serializable;
import java.util.ArrayList;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "users")
public class HstsUser implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	String userId;
	String userPassword;
	int userType; // 1.Student 2.Teacher 3.Principal
	String fullName;
	boolean connectionStatus;
	
	ArrayList<String> subjects; //Teacher's
	
	// Math - 01
	// CS - 43
	// Biology - 78
	
	ArrayList<String> courses; //Teacher's
	
	// Calculus - 22
	// Algebra 101 - 13
	// Introduction to Probability - 10
	
	// Introduction to CS - 19
	// Data structures - 65
	// OS - 03
	
	// Anatomy - 72
	// Stem Cells - 42
	// Biostructure - 16
	
	public HstsUser() {}
	
	public HstsUser(String userId, String userPassword, int userType, ArrayList<String> subjects, ArrayList<String> courses, 
			String fullName, boolean connectionStatus)
	{
		super();
		this.userId = userId;
		this.userPassword = userPassword;
		this.userType = userType;
		this.subjects = subjects;
		this.courses = courses;
		this.fullName = fullName;
		this.connectionStatus = connectionStatus;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserPassword() {
		return userPassword;
	}

	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}

	public int getUserType() {
		return userType;
	}

	public void setUserType(int userType) {
		this.userType = userType;
	}

	public ArrayList<String> getSubjects() {
		return subjects;
	}

	public void setSubjects(ArrayList<String> subjects) {
		this.subjects = subjects;
	}

	public ArrayList<String> getCourses() {
		return courses;
	}

	public void setCourses(ArrayList<String> courses) {
		this.courses = courses;
	}
	
	public boolean getConnectionStatus() {
		return connectionStatus;
	}

	public void setConnectionStatus(boolean connectionStatus) {
		this.connectionStatus = connectionStatus;
	}
	
}