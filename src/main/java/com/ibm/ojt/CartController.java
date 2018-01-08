package com.ibm.ojt;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurerAdapter;
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
@Configuration
@RequestMapping("/cart")
public class CartController extends RepositoryRestConfigurerAdapter{
	
	@Autowired
	MongoTemplate mongoTemplate;
	
//	@Autowired
//	CartRepository cartRepository;
	
	public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
		config.exposeIdsFor(Cart.class);
	}
	
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public void createCart(@RequestBody Cart cart) {
		Query query = new Query().addCriteria(Criteria.where("customerId").is(cart.getCustomerId()));
		Cart _cart = mongoTemplate.findOne(query, Cart.class, "cart");
//		Cart _cart = cartRepository.findOne(cart.getCustomerId());
		if (_cart == null) {
			_cart = new Cart();
			_cart.setCustomerId(cart.getCustomerId());
			_cart.setCartItems(cart.getCartItems());
			_cart.setStatus("OP");
			mongoTemplate.save(_cart, "cart");
//			cartRepository.save(cart);
		}
	}
	
	@GetMapping
	public List<Cart> findAllCarts() {
		return mongoTemplate.findAll(Cart.class, "cart");
//		return cartRepository.findAll();
	}
	
	@DeleteMapping("/{_id}")
	public void deleteCart(@PathVariable String _id) {
		Query query = new Query(Criteria.where("_id").is(_id));
		mongoTemplate.findAllAndRemove(query, "cart");
	}

	@GetMapping("/{_id}")
	public Cart findByCartId(@PathVariable String _id) {
		Query query = new Query().addCriteria(Criteria.where("_id").is(_id));
		Cart _cart = mongoTemplate.findOne(query, Cart.class, "cart");
		return _cart;
	}
	
	//adds a new item to cart
	//Problem_Area: What if same item but different sizes???
	//	Answer: Dapat different UPCs for each size
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, value = "/{_id}/add")
	public void addToCart(@RequestBody CartItem cartItem, @PathVariable String _id) {
		Criteria custCriteria = Criteria.where("_id").is(_id);
		Query query = new Query(new Criteria().andOperator(custCriteria, Criteria.where("cartItems.prodCode").nin(cartItem.getProdCode())));
//		Cart _cart = mongoTemplate.findOne(query, Cart.class, "cart");
		Update update = new Update().addToSet("cartItems", cartItem);
//		if (_cart == null) { // if new item is not in the cart
//			System.out.println("NISULOD KO NGARI" + _cart);
//			query = new Query(new Criteria().andOperator(custCriteria, Criteria.where("cartItems.prodCode").in(cartItem.getProdCode())));
//		}
		mongoTemplate.updateFirst(query, update, "cart");
	}
	
	//Updates the quantity/size of item
	//Problem_area: DI SYA MOUPDATE!!!
	@PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, value = "/{_id}/update")
	public void updateCartItem(@RequestBody CartItem cartItem, @PathVariable String _id) {
		Criteria custCriteria = Criteria.where("_id").is(_id);
		Query query = new Query(new Criteria().andOperator(custCriteria, Criteria.where("cartItems.prodCode").in(cartItem.getProdCode())));
		Update update = new Update().addToSet("cartItems", cartItem);
		mongoTemplate.updateFirst(query, update, "cart");
	}
	
	//deletes item from cart
	@DeleteMapping(consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, value="/{_id}/delete")
	public void deleteCartItem(@RequestBody CartItem cartItem, @PathVariable String _id) {
		Query query = new Query().addCriteria(Criteria.where("_id").is(_id).and("cartItems.prodCode").in(cartItem.getProdCode()));
		Cart _cart = mongoTemplate.findOne(query, Cart.class, "cart");
		if (_cart != null) { // if new item is not in the cart
			Update update = new Update().pull("cartItems", cartItem);
			mongoTemplate.updateFirst(query, update, "cart");
		}
	}
	
	//deletes all items from cart
	@PutMapping(value="/{_id}/delete/all")
	public void emptyCart(@PathVariable String _id) {
		Query query = new Query().addCriteria(Criteria.where("_id").is(_id));
		Cart _cart = mongoTemplate.findOne(query, Cart.class, "cart");
		_cart.getCartItems().clear();
		Update update = new Update().set("cartItems", _cart.getCartItems());
		mongoTemplate.updateFirst(query, update, "cart");
	}
	
	//updates status of cart to "CL" when customer checks out
	@PutMapping("/{_id}/checkout")
	public void closeCart(@PathVariable String _id) {
		Query query = new Query().addCriteria(Criteria.where("id").is(_id));
		Update update = new Update().set("status", "CL");
		mongoTemplate.updateFirst(query, update, "cart");
	}
}