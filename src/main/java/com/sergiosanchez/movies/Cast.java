package com.sergiosanchez.movies;

/**
 * Clase objeto que maneja la información de un actor/actriz
 * @author Sergio Sanchez
 *
 */
public class Cast {
	
	int id;
	String character;
	String actor;
	String img;
	
	/**
	 * @param id
	 * @param character
	 * @param img
	 */
	public Cast(int id, String character, String actor, String img) {
		super();
		this.id = id;
		this.character = character;
		this.actor = actor;
		this.img = img;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the character
	 */
	public String getCharacter() {
		return character;
	}

	/**
	 * @param character the character to set
	 */
	public void setCharacter(String character) {
		this.character = character;
	}

	/**
	 * @return the actor
	 */
	public String getActor() {
		return actor;
	}

	/**
	 * @param actor the actor to set
	 */
	public void setActor(String actor) {
		this.actor = actor;
	}

	/**
	 * @return the img
	 */
	public String getImg() {
		return img;
	}

	/**
	 * @param img the img to set
	 */
	public void setImg(String img) {
		this.img = img;
	}

}
