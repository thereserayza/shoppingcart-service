package com.ibm.ojt;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.repository.query.Param;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping("/cart")
public class CartController{
	
	@Autowired
	MongoTemplate mongoTemplate;
	
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public void createCart(@RequestBody Cart cart) {
		Query query = new Query().addCriteria(Criteria.where("customerId").is(cart.getCustomerId()));
		Cart _cart = mongoTemplate.findOne(query, Cart.class, "cart");
		if (_cart == null) {
			_cart = new Cart();
			_cart.setCustomerId(cart.getCustomerId());
			_cart.setCartItems(cart.getCartItems());
			_cart.setStatus("OP");
			mongoTemplate.save(_cart, "cart");
		}
	}
	
	@GetMapping
	public List<Cart> findAllCarts() {
		return mongoTemplate.findAll(Cart.class, "cart");
	}
	
	@DeleteMapping
	public void deleteCart(@Param("customerId") String customerId) {
		Query query = new Query(Criteria.where("customerId").is(customerId));
		mongoTemplate.findAllAndRemove(query, "cart");
	}

	@GetMapping
	public Cart findByCartId(@Param("customerId") String customerId) {
		Query query = new Query().addCriteria(Criteria.where("customerId").is(customerId));
		Cart _cart = mongoTemplate.findOne(query, Cart.class, "cart");
		return _cart;
	}
	
	//adds a new item to cart
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, value = "/add")
	public void addToCart(@RequestBody CartItem cartItem, @Param("customerId") String customerId) {
		Criteria custCriteria = Criteria.where("customerId").is(customerId);
		Query query = new Query(new Criteria().andOperator(custCriteria, Criteria.where("cartItems.prodCode").nin(cartItem.getProdCode())));
		Update update = new Update().addToSet("cartItems", cartItem);
		mongoTemplate.updateFirst(query, update, "cart");
	}
	
	//Updates the quantity/size of item
	@PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, value = "/update")
	public void updateCartItem(@RequestBody CartItem cartItem, @Param("customerId") String customerId) {
		Criteria custCriteria = Criteria.where("customerId").is(customerId);
		Query query = new Query(new Criteria().andOperator(custCriteria, Criteria.where("cartItems.prodCode").in(cartItem.getProdCode())));
		Update update = new Update().set("cartItems.$.itemQty", cartItem.getItemQty());
		System.out.println(mongoTemplate.updateFirst(query, update, "cart"));
	}
	
	//deletes item from cart
	@DeleteMapping(consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, value="/delete")
	public void deleteCartItem(@RequestBody CartItem cartItem, @Param("customerId") String customerId) {
		Query query = new Query().addCriteria(Criteria.where("customerId").is(customerId).and("cartItems.prodCode").in(cartItem.getProdCode()));
		Cart _cart = mongoTemplate.findOne(query, Cart.class, "cart");
		if (_cart != null) { // if new item is not in the cart
			Update update = new Update().pull("cartItems", cartItem);
			mongoTemplate.updateFirst(query, update, "cart");
		}
	}
	
	//deletes all items from cart
	@PutMapping(value="/delete/all")
	public void emptyCart(@Param("customerId") String customerId) {
		Query query = new Query().addCriteria(Criteria.where("customerId").is(customerId));
		Cart _cart = mongoTemplate.findOne(query, Cart.class, "cart");
		_cart.getCartItems().clear();
		Update update = new Update().set("cartItems", _cart.getCartItems());
		mongoTemplate.updateFirst(query, update, "cart");
	}
	
	//updates status of cart to "CL" when customer checks out
	@PutMapping("/checkout")
	public void closeCart(@Param("customerId") String customerId) {
		Query query = new Query().addCriteria(Criteria.where("customerId").is(customerId));
		Update update = new Update().set("status", "CL");
		mongoTemplate.updateFirst(query, update, "cart");
	}
}