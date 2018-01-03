package com.ibm.ojt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
//import org.springframework.data.repository.query.Param;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cart")
public class CartController {
	
	@Autowired
	MongoTemplate mongoTemplate;
	
//	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
//	public void addToCart(@RequestBody Cart cart) {
//		cartRepository.save(cart);
//	}
	
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public void addToCart(@RequestBody Cart cart) {
		Query query = new Query();
		query.addCriteria(Criteria.where("customerId").is(cart.getCustomerId()));
		Update update = new Update();
		update.addToSet("item", cart.getCartItems()); //not the solution
		mongoTemplate.upsert(query, update, "cart");
	}
}