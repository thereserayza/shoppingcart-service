package com.ibm.ojt;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;

public class Cart {
	@Id
	private String id;
	
	private String customerId;
	private List<CartItem> cartItems = new ArrayList<CartItem>();
	private String status;
	
	public String getCustomerId() {
		return customerId;
	}
	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}
	public List<CartItem> getCartItems() {
		return cartItems;
	}
	public void setCartItems(List<CartItem> cartItems) {
		this.cartItems = cartItems;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
}
