package com.github.model;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonFormat;

public class Transaction {
	
	private String name;
	private Double amount;
	private Instant time;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Double getAmount() {
		return amount;
	}
	public void setAmount(Double amount) {
		this.amount = amount;
	}
	public Instant getTime() {
		return time;
	}
	public void setTime(Instant time) {
		this.time = time;
	}
	
	public Transaction withName(String name) {
		this.name = name;
		return this;
	}
	public Transaction withAmount(Double amount) {
		this.amount = amount;
		return this;
	}
	public Transaction withTime(Instant time) {
		this.time = time;
		return this;
	}
	
	
	
	
	

}
