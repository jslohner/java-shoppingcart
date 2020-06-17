package com.lambdaschool.shoppingcart.controllers;

import com.lambdaschool.shoppingcart.exceptions.ResourceNotFoundException;
import com.lambdaschool.shoppingcart.handlers.HelperFunctions;
import com.lambdaschool.shoppingcart.models.Cart;
import com.lambdaschool.shoppingcart.models.Product;
import com.lambdaschool.shoppingcart.models.User;
import com.lambdaschool.shoppingcart.services.CartService;
import com.lambdaschool.shoppingcart.services.UserAuditing;
import com.lambdaschool.shoppingcart.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/carts")
public class CartController {
	@Autowired
	private CartService cartService;

	@Autowired
	private UserService userService;

	@Autowired
	private UserAuditing userAuditing;

	@Autowired
	private HelperFunctions helper;

	@GetMapping(value = "/user", produces = {"application/json"})
	public ResponseEntity<?> listAllCarts() {
		User user = userService.findByName(userAuditing.getCurrentAuditor().get());
		List<Cart> myCarts = cartService.findAllByUserId(user.getUserid());
		return new ResponseEntity<>(myCarts, HttpStatus.OK);
	}

	@PreAuthorize("hasAnyRole('ADMIN')")
	@GetMapping(value = "/cart/{cartId}",
		produces = {"application/json"})
	public ResponseEntity<?> getCartById(
		@PathVariable
			Long cartId) {
		Cart p = cartService.findCartById(cartId);
		return new ResponseEntity<>(p,
			HttpStatus.OK);
	}

	@PostMapping(value = "/create/product/{productid}")
	public ResponseEntity<?> addNewCart(@PathVariable long productid) {
		User dataUser = userService.findByName(userAuditing.getCurrentAuditor().get());

		Product dataProduct = new Product();
		dataProduct.setProductid(productid);

		cartService.save(dataUser, dataProduct);
		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@PutMapping(value = "/update/cart/{cartid}/product/{productid}")
	public ResponseEntity<?> updateCart(@PathVariable long cartid,
										@PathVariable long productid) {
		Cart dataCart = new Cart();
		dataCart.setCartid(cartid);

		Product dataProduct = new Product();
		dataProduct.setProductid(productid);

		if (helper.isAuthorizedToMakeChange(userAuditing.getCurrentAuditor().get())) {
			cartService.save(dataCart, dataProduct);
			return new ResponseEntity<>(HttpStatus.OK);
		} else {
			throw new ResourceNotFoundException("This user is not authorized to make change");
		}
	}

	@DeleteMapping(value = "/delete/cart/{cartid}/product/{productid}")
	public ResponseEntity<?> deleteFromCart(@PathVariable long cartid,
											@PathVariable long productid) {
		Cart dataCart = new Cart();
		dataCart.setCartid(cartid);

		Product dataProduct = new Product();
		dataProduct.setProductid(productid);

		cartService.delete(dataCart, dataProduct);
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
