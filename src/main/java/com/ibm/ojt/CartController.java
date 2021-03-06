package com.ibm.ojt;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.DBObject;

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
			_cart.setTotalPrice(0.00);
			_cart.setStatus("OP");
			mongoTemplate.save(_cart, "cart");
		}
	}
	
	@GetMapping
	public List<Cart> findAllCarts() {
		return mongoTemplate.findAll(Cart.class, "cart");
	}
	
	@DeleteMapping("/{customerId}")
	public void deleteCart(@PathVariable String customerId) {
		Query query = new Query(Criteria.where("customerId").is(customerId));
		mongoTemplate.findAllAndRemove(query, "cart");
	}
	
	@GetMapping("/{customerId}")
	public Cart findByCustomerId(@PathVariable String customerId) {
		Query query = new Query().addCriteria(Criteria.where("customerId").is(customerId));
		Cart _cart = mongoTemplate.findOne(query, Cart.class, "cart");
		return _cart;
	}
	
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, value = "/{customerId}/add")
	public void addToCart(@RequestBody CartItem cartItem, @PathVariable String customerId) {
		Criteria custCriteria = Criteria.where("customerId").is(customerId);
		Query query = new Query(new Criteria().andOperator(custCriteria, Criteria.where("cartItems.prodCode").nin(cartItem.getProdCode())));
		Cart _cart = mongoTemplate.findOne(query, Cart.class, "cart");
		Update update = new Update();
		if (_cart != null) {
			update.addToSet("cartItems", cartItem);
		} else {
			query = new Query(new Criteria().andOperator(custCriteria, Criteria.where("cartItems.prodCode").in(cartItem.getProdCode())));
			_cart = mongoTemplate.findOne(query, Cart.class, "cart");
			update.inc("cartItems.$.itemQty", cartItem.getItemQty());
			update.inc("cartItems.$.subtotal", cartItem.getSubtotal());
		}
		update.set("totalPrice", _cart.getTotalPrice() + cartItem.getSubtotal());
		mongoTemplate.updateFirst(query, update, "cart");
	}
	
	@PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, value = "/{customerId}/update")
	public void updateCartItem(@RequestBody CartItem cartItem, @PathVariable String customerId) {
		Criteria custCriteria = Criteria.where("customerId").is(customerId);
		Query query = new Query(new Criteria().andOperator(custCriteria, Criteria.where("cartItems.prodCode").in(cartItem.getProdCode())));
		Update update = new Update().set("cartItems.$.itemQty", cartItem.getItemQty());
		update.set("cartItems.$.subtotal", cartItem.getSubtotal());
		mongoTemplate.updateFirst(query, update, "cart");
		Aggregation agg = Aggregation.newAggregation(Aggregation.match(custCriteria), Aggregation.unwind("cartItems"), Aggregation.group().sum("cartItems.subtotal").as("subtotal"));
		AggregationResults<DBObject> results = mongoTemplate.aggregate(agg, "cart", DBObject.class);
		Update _update = new Update().set("totalPrice", results.getUniqueMappedResult().get("subtotal"));
		mongoTemplate.updateFirst(query, _update, "cart");
	}

	@DeleteMapping(value="/{customerId}/delete/{prodcode}")
	public void deleteCartItem(@PathVariable String customerId, @PathVariable String prodcode) {
		Criteria custCriteria = Criteria.where("customerId").is(customerId);
		Query query = new Query().addCriteria(custCriteria.and("cartItems.prodCode").in(prodcode));
		Cart _cart = mongoTemplate.findOne(query, Cart.class, "cart");
		CartItem _cartItem = null;
		for (CartItem i : _cart.getCartItems()) {
			if (i.getProdCode().equals(prodcode)) {
				_cartItem = i;
				break;
			}
		}
		if (_cart != null && _cartItem != null) {
			Update update = new Update().pull("cartItems", _cartItem);
			update.set("totalPrice", _cart.getTotalPrice() - _cartItem.getSubtotal());
			mongoTemplate.updateFirst(query, update, "cart");
		}
	}
	
	@DeleteMapping(value="/{customerId}/delete/all")
	public void emptyCart(@PathVariable String customerId) {
		Query query = new Query().addCriteria(Criteria.where("customerId").is(customerId));
		Cart _cart = mongoTemplate.findOne(query, Cart.class, "cart");
		_cart.getCartItems().clear();
		Update update = new Update().set("cartItems", _cart.getCartItems());
		update.set("totalPrice", 0.00);
		mongoTemplate.updateFirst(query, update, "cart");
	}
}