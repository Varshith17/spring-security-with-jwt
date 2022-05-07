package com.digitalhonors.authorization.controller;

import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.digitalhonors.authorization.config.JwtTokenUtil;
import com.digitalhonors.authorization.payloadData.RequestData;
import com.digitalhonors.authorization.payloadData.ResponseData;
import com.digitalhonors.authorization.service.CustomerDetailsService;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JwtTokenUtil jwtTokenUtil;

	@Autowired
	private CustomerDetailsService customerDetailsService;

	@RequestMapping(value = "/authenticate", method = RequestMethod.POST)
	public ResponseEntity<?> createAuthenticationToken(@RequestBody RequestData request) throws Exception {

		try {
			authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
		} catch (BadCredentialsException e) {
			throw new Exception("INVALID_CREDENTIALS", e);
		}

		final UserDetails userDetails = customerDetailsService.loadUserByUsername(request.getUsername());

		final String token = jwtTokenUtil.generateToken(userDetails);

		return ResponseEntity.ok(new ResponseData(token));
	}

	@GetMapping("/authorize")
	public ResponseEntity<?> validateAndReturnUser(@RequestHeader("Authorization") String token) {
		UserDetails user = customerDetailsService.loadUserByUsername(jwtTokenUtil.getUsernameFromToken(token));
		if (jwtTokenUtil.validateToken(token, user)) {
			return new ResponseEntity<>(true, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(false, HttpStatus.FORBIDDEN);
		}
	}

}
