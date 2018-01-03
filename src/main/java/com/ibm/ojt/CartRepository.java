package com.ibm.ojt;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="cartdetail", path="cart")
public interface CartRepository extends MongoRepository<Cart, String>{
	
	List<Cart> findByCustomerId(@Param("custid") String customerId);
}
