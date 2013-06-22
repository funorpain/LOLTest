package com.funorpain.loltest.model;

import java.util.Set;

public class Item {
	private int id;
	private String name;
	private String attr;
	private int price;
	private Set<String> filters;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAttr() {
		return attr;
	}

	public void setAttr(String attr) {
		this.attr = attr;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public Set<String> getFilters() {
		return filters;
	}

	public void setFilters(Set<String> filters) {
		this.filters = filters;
	}
}
