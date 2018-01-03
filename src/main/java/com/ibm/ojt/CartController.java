package com.ibm.ojt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cart/{cartId}")
public class CartController {
	
	@Autowired
	CartRepository cartRepository;
	
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public void addToCart(@RequestBody Cart cart) {
		cartRepository.save(cart);
	}
}
