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
		Cart _cart = cartRepository.findOne(cart.getCustomerId());
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
		if (_cart == null) {
			_cart = new Cart();
			_cart.setCustomerId(customerId);
			_cart.setStatus("OP");
			createCart(_cart);
		}
		return _cart;
	}
	
	//adds a new item to cart or updates the quantity/size of item
	//Problem_Area: What if same item but different sizes???
	//	Answer: Dapat different UPCs for each size
	@PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, value = "/{customerId}/addupdate")
	public void addToCart(@RequestBody CartItem cartItem, @PathVariable String customerId) {
		Criteria custCriteria = Criteria.where("customerId").is(customerId);
		Query query = new Query(new Criteria().andOperator(custCriteria, Criteria.where("cartItems.prodCode").nin(cartItem.getProdCode())));
		Cart _cart = mongoTemplate.findOne(query, Cart.class, "cart");
		System.out.println(_cart);
		Update update = new Update();
		update.addToSet("cartItems", cartItem);
		if (_cart == null) { // if new item is not in the cart
			System.out.println("CART == NULL");
			query = new Query(new Criteria().andOperator(custCriteria, Criteria.where("cartItems.prodCode").in(cartItem.getProdCode())));
		}
		else {
			System.out.println("CART != NULL");
		}
		System.out.println(mongoTemplate.updateFirst(query, update, "cart"));
	}
	
	//deletes item
	@PutMapping(consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, value="/{customerId}/delete")
	public void deleteCartItem(@RequestBody CartItem cartItem, @PathVariable String customerId) {
		Query query = new Query();
		query.addCriteria(Criteria.where("customerId").is(customerId).and("cartItems.prodCode").in(cartItem.getProdCode()));
		Cart _cart = mongoTemplate.findOne(query, Cart.class, "cart");
		Update update = new Update();
		if (_cart != null) { // if new item is not in the cart
			System.out.println("DELETE ITEM FROM CART -");
			update.pull("cartItems", cartItem);
			System.out.println(mongoTemplate.updateFirst(query, update, "cart"));
		}
	}
	
	//updates status of cart (e.g. When customer checks out cart and finishes the process, cart is closed for updates
	//		until customer adds a new item to cart
//	@PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, value = "/{customerId}")
//	public void updateCartStatus(@PathVariable String customerId) {
//		Query query = new Query();
//		query.addCriteria(Criteria.where("customerId").is(customerId));
//		Update update = new Update();
//		update.set("status", "");
//		mongoTemplate.updateFirst(query, update, "cart");
//	}
}