package com.ysh.phoneassistant.domain;

public class ContactsInfo {
	private String name;
	private String phoneNum;
	
	public ContactsInfo() {}
	
	public ContactsInfo(String name, String phoneNum) {
		super();
		this.name = name;
		this.phoneNum = phoneNum;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPhoneNum() {
		return phoneNum;
	}
	public void setPhoneNum(String phoneNum) {
		this.phoneNum = phoneNum;
	}
	
	@Override
	public String toString() {
		
		return this.name +"------"+ this.phoneNum;
		
	}
	
}
