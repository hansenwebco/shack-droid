package com.stonedonkey.shackdroid;

import java.io.Serializable;

public class ShackProfile implements Serializable{

	private static final long serialVersionUID = 1L;
	private String error;
	
	private String shackname;

	private String join_date;
	private String firstpost_date;
	private String mostrecentpost_date;
	private String postcount;
	private String postsperday;
	private String posterclass;
	private String lastupdate;
	
	private String is_mod;
	private String is_subscriber;
	
	private String sex;
	private String birthdate;
	private String age;
	private String location;
	private String homepage;
	
	private String yahoo;
	private String wii;
	private String xfire;
	private String xboxlive;
	private String playstation_network;
	private String aim;
	private String gtalk;
	private String msn;
	private String icq;
	private String steam;
	
	private String user_bio;
	
	public ShackProfile(String shackname, String location, String user_bio,
						String homepage, String join_date, String firstpost_date,
						String mostrecentpost_date, String postcount, String postsperday,
						String posterclass, String lastupdate, String is_mod,
						String is_subscriber, String sex, String birthdate, String age,
						String yahoo, String wii, String xfire, String xboxlive,
						String playstation_network, String aim, String gtalk, String msn,
						String icq, String steam) {
		super();
		this.shackname = shackname;
		this.location = location;
		this.user_bio = user_bio;
		this.homepage = homepage;
		this.join_date = join_date;
		this.firstpost_date = firstpost_date;
		this.mostrecentpost_date = mostrecentpost_date;
		this.postcount = postcount;
		this.postsperday = postsperday;
		this.posterclass = posterclass;
		this.lastupdate = lastupdate;
		this.is_mod = is_mod;
		this.is_subscriber = is_subscriber;
		this.sex = sex;
		this.birthdate = birthdate;
		this.age = age;
		this.yahoo = yahoo;
		this.wii = wii;
		this.xfire = xfire;
		this.xboxlive = xboxlive;
		this.playstation_network = playstation_network;
		this.aim = aim;
		this.gtalk = gtalk;
		this.msn = msn;
		this.icq = icq;
		this.steam = steam;
	}

	public ShackProfile()
	{
	}
		
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	
	public String getShackname() {
		return shackname;
	}
	public void setShackname(String shackname) {
		this.shackname = shackname;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getUser_bio() {
		return user_bio;
	}
	public void setUser_bio(String user_bio) {
		this.user_bio = user_bio;
	}
	public String getHomepage() {
		return homepage;
	}
	public void setHomepage(String homepage) {
		this.homepage = homepage;
	}
	public String getJoin_date() {
		return join_date;
	}
	public void setJoin_date(String join_date) {
		this.join_date = join_date;
	}
	public String getFirstpost_date() {
		return firstpost_date;
	}
	public void setFirstpost_date(String firstpost_date) {
		this.firstpost_date = firstpost_date;
	}
	public String getMostrecentpost_date() {
		return mostrecentpost_date;
	}
	public void setMostrecentpost_date(String mostrecentpost_date) {
		this.mostrecentpost_date = mostrecentpost_date;
	}
	public String getPostcount() {
		return postcount;
	}
	public void setPostcount(String postcount) {
		this.postcount = postcount;
	}
	public String getPostsperday() {
		return postsperday;
	}
	public void setPostsperday(String postsperday) {
		this.postsperday = postsperday;
	}
	public String getPosterclass() {
		return posterclass;
	}
	public void setPosterclass(String posterclass) {
		this.posterclass = posterclass;
	}
	public String getLastupdate() {
		return lastupdate;
	}
	public void setLastupdate(String lastupdate) {
		this.lastupdate = lastupdate;
	}
	public String getIs_mod() {
		return is_mod;
	}
	public void setIs_mod(String is_mod) {
		this.is_mod = is_mod;
	}
	public String getIs_subscriber() {
		return is_subscriber;
	}
	public void setIs_subscriber(String is_subscriber) {
		this.is_subscriber = is_subscriber;
	}
	public String getSex() {
		return sex;
	}
	public void setSex(String sex) {
		this.sex = sex;
	}
	public String getBirthdate() {
		return birthdate;
	}
	public void setBirthdate(String birthdate) {
		this.birthdate = birthdate;
	}
	public String getAge() {
		return age;
	}
	public void setAge(String age) {
		this.age = age;
	}
	public String getYahoo() {
		return yahoo;
	}
	public void setYahoo(String yahoo) {
		this.yahoo = yahoo;
	}
	public String getWii() {
		return wii;
	}
	public void setWii(String wii) {
		this.wii = wii;
	}
	public String getXfire() {
		return xfire;
	}
	public void setXfire(String xfire) {
		this.xfire = xfire;
	}
	public String getXboxlive() {
		return xboxlive;
	}
	public void setXboxlive(String xboxlive) {
		this.xboxlive = xboxlive;
	}
	public String getPlaystation_network() {
		return playstation_network;
	}
	public void setPlaystation_network(String playstation_network) {
		this.playstation_network = playstation_network;
	}
	public String getAim() {
		return aim;
	}
	public void setAim(String aim) {
		this.aim = aim;
	}
	public String getGtalk() {
		return gtalk;
	}
	public void setGtalk(String gtalk) {
		this.gtalk = gtalk;
	}
	public String getMsn() {
		return msn;
	}
	public void setMsn(String msn) {
		this.msn = msn;
	}
	public String getIcq() {
		return icq;
	}
	public void setIcq(String icq) {
		this.icq = icq;
	}
	public String getSteam() {
		return steam;
	}
	public void setSteam(String steam) {
		this.steam = steam;
	}
	
}