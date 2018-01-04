package com.ibm.ojt;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cart")
public class CartController {
	
	@Autowired
	MongoTemplate mongoTemplate;
	
	@Autowired
	CartRepository cartRepository;
	
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public void createCart(@RequestBody Cart cart) {
		Cart _cart = findByCustomerId(cart.getCustomerId());
		if (_cart != null) {
			System.out.println("CUSTOMER ID EXISTS");
		}
		else {
			cartRepository.save(cart);
			System.out.println("CUSTOMER ID DOES NOT EXIST");
		}
	}
	
	@GetMapping
	public List<Cart> findAllCarts() {
		return cartRepository.findAll();
	}
	
	@DeleteMapping("/{customerId}")
	public void deleteCart(@PathVariable String customerId) {
		Query query = new Query(Criteria.where("customerId").is(customerId));
		System.out.println(mongoTemplate.findAllAndRemove(query, "cart"));
	}

	@GetMapping("/{customerId}")
	public Cart findByCustomerId(@PathVariable String customerId) {
		Query query = new Query();
		query.addCriteria(Criteria.where("customerId").is(customerId));
		Cart _cart = mongoTemplate.findOne(query, Cart.class, "cart");
		System.out.println("FINDBYCUSTOMERID\n" + _cart);
		return _cart;
	}
	
	//adds a new item to cart or updates the quantity/size of item
	//Problem_Area: How to and/or-operator the update logic
	@PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, value = "/{customerId}")
	public void addToCart(@RequestBody CartItem cartItem, @PathVariable String customerId) {
		Criteria custCriteria = Criteria.where("customerId").is(customerId);
		Criteria prodCriteria = Criteria.where("prodCode").exists(true);
		Query query = new Query(new Criteria().andOperator(custCriteria, prodCriteria));
		mongoTemplate.findOne(query, Cart.class, "cart");
//		Query query = new Query();
//		query.addCriteria(Criteria.where("customerId").is(customerId).and("prodCode").exists(true));
		Update update = new Update();
		update.addToSet("cartItems", cartItem);
		System.out.println(mongoTemplate.updateFirst(query, update, "cart"));
	}
	
	//updates status of cart (e.g. When customer checks out cart and finishes the process, cart is closed for updates
	//		until customer adds a new item to cart
//	@PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, value = "/{customerId}")
//	public void updateCartStatus(@PathVariable String customerId) {
//		Query query = new Query();
//		query.addCriteria(Criteria.where("customerId").is(customerId));
//		Update update = new Update();
//		update.addToSet("status", "");
//		mongoTemplate.updateFirst(query, update, "cart");
//	}
}