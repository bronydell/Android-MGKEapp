package ru.equestriadev.arch;

import java.util.ArrayList;

public class Day {
	private String date;
	private ArrayList<Group> groups;

	/**
	 * Getter for date
	 * 
	 * @return Date of recording
	 */
	public String getDate() {
		return date;
	}

	/**
	 * Setter for date
	 * 
	 * @param Date
	 *            of recording
	 */
	public void setDate(String date) {
		this.date = date;
	}

	/**
	 * Getter for groups
	 * 
	 * @return Group array
	 */
	public ArrayList<Group> getGroups() {
		return groups;
	}

	/**
	 * Setter for groups
	 * 
	 * @param Group
	 *            array
	 */
	public void setGroups(ArrayList<Group> groups) {
		this.groups = groups;
	}

}
