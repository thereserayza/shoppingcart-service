package com.ibm.ojt;

//import java.util.List;

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
	
//	@Autowired
//	CartRepository cartRepository;
	
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public void createCart(@RequestBody Cart cart) {
//		Cart _cart = cartRepository.findOne(cart.getCustomerId());
//		if (_cart == null) {
//			cartRepository.save(cart);
//		}
	}
	
//	@GetMapping
//	public List<Cart> findAllCarts() {
//		return cartRepository.findAll();
//	}
	
	@DeleteMapping("/{customerId}")
	public void deleteCart(@PathVariable String customerId) {
		Query query = new Query(Criteria.where("customerId").is(customerId));
		mongoTemplate.findAllAndRemove(query, "cart");
	}

	@GetMapping("/{customerId}")
	public Cart findByCustomerId(@PathVariable String customerId) {
		Query query = new Query().addCriteria(Criteria.where("customerId").is(customerId));
		Cart _cart = mongoTemplate.findOne(query, Cart.class, "cart");
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
		Update update = new Update().addToSet("cartItems", cartItem);
		if (_cart == null) { // if new item is not in the cart
			query = new Query(new Criteria().andOperator(custCriteria, Criteria.where("cartItems.prodCode").in(cartItem.getProdCode())));
		}
		mongoTemplate.updateFirst(query, update, "cart");
	}
	
	//deletes item from cart
	@PutMapping(consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, value="/{customerId}/delete")
	public void deleteCartItem(@RequestBody CartItem cartItem, @PathVariable String customerId) {
		Query query = new Query().addCriteria(Criteria.where("customerId").is(customerId).and("cartItems.prodCode").in(cartItem.getProdCode()));
		Cart _cart = mongoTemplate.findOne(query, Cart.class, "cart");
		if (_cart != null) { // if new item is not in the cart
			Update update = new Update().pull("cartItems", cartItem);
			mongoTemplate.updateFirst(query, update, "cart");
		}
	}
	
	//deletes all items from cart
	@PutMapping(value="/{customerId}/delete/all")
	public void emptyCart(@PathVariable String customerId) {
		Query query = new Query().addCriteria(Criteria.where("customerId").is(customerId));
		Cart _cart = mongoTemplate.findOne(query, Cart.class, "cart");
		_cart.getCartItems().clear();
		Update update = new Update().set("cartItems", _cart.getCartItems());
		mongoTemplate.updateFirst(query, update, "cart");
	}
	
	//updates status of cart to "CL" when customer checks out
	@PutMapping("/{customerId}/checkout")
	public void closeCart(@PathVariable String customerId) {
		Query query = new Query().addCriteria(Criteria.where("customerId").is(customerId));
		Update update = new Update().set("status", "CL");
		mongoTemplate.updateFirst(query, update, "cart");
	}
}