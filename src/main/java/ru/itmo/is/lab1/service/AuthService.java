package ru.itmo.is.lab1.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import ru.itmo.is.lab1.security.JwtService;
import ru.itmo.is.lab1.web.dto.LoginRequest;
import ru.itmo.is.lab1.web.dto.LoginResponse;

@Service
public class AuthService {

	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;

	public AuthService(AuthenticationManager authenticationManager, JwtService jwtService) {
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
	}

	public LoginResponse login(LoginRequest request) {
		authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(request.username(), request.password()));
		String token = jwtService.generateToken(request.username());
		return new LoginResponse(token, "Bearer", jwtService.expiresInSeconds(), request.username());
	}

}
