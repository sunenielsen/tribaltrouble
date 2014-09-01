package com.oddlabs.registration;

import java.io.Serializable;

public final strictfp class RegistrationInfo implements Serializable {
	private final static long serialVersionUID = 2;

	private final long key;
	private final String reg_key;
	private final String reg_email;
	private final String reg_time;
	private final String name;
	private final String address1;
	private final String address2;
	private final String zip;
	private final String city;
	private final String state;
	private final String country;
	private final long time_stamp;
	
	public RegistrationInfo(long key, String reg_key, String reg_email, String reg_time, String name, String address1, String address2, String zip, String city, String state, String country, long time_stamp) {
		this.key = key;
		this.reg_key = reg_key;
		this.reg_email = reg_email;
		this.reg_time = reg_time;
		this.name = name;
		this.address1 = address1;
		this.address2 = address2;
		this.zip = zip;
		this.city = city;
		this.state = state;
		this.country = country;
		this.time_stamp = time_stamp;
	}

	public final String getRegTime() {
		return reg_time;
	}
	
	public final String getRegEmail() {
		return reg_email;
	}
	
	public final long getTimeStamp() {
		return time_stamp;
	}
	
	public final long getKey() {
		return key;
	}

	public final String toString() {
		return key + " " + reg_key + " " + name + " " + address1 + " " + address2 + " " + zip + " " + city + " " + state + " " + country;
	}

	public final String getRegKey() {
		return reg_key;
	}

	public final String getName() {
		return name;
	}

	public final String getAddress1() {
		return address1;
	}

	public final String getAddress2() {
		return address2;
	}

	public final String getZip() {
		return zip;
	}

	public final String getCity() {
		return city;
	}

	public final String getState() {
		return state;
	}

	public final String getCountry() {
		return country;
	}
}
