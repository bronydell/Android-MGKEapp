//Author: BRONYDELL
//DATE: 31.05.2016
package ru.equestriadev.arch;

import java.util.ArrayList;

public class Group implements Comparable<Group>{
	private String title;
	private ArrayList<Lesson> lessons;
	private boolean isFavorite;
	
	/**
	 * Setter for group title
	 * @param Group number
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Getter for group title
	 * @return Group number
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Setter for lessons array
	 * @param lessons array
	 */
	public void setLessons(ArrayList<Lesson> lessons) {
		this.lessons = lessons;
	}

	/**
	 * Getter for lessons array
	 * @return lessons array
	 */
	public ArrayList<Lesson> getLessons() {
		return lessons;
	}


	public boolean isFavorite() {
		return isFavorite;
	}

	public void setIsFavorite(boolean isFavorite) {
		this.isFavorite = isFavorite;
	}

	@Override
	public int compareTo(Group another) {
		if(another.isFavorite())
			return 1;
		else
			return 0;
	}

}
