package ru.itmo.is.lab1.security;

import java.nio.charset.StandardCharsets;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtDecoder jwtDecoder;

	public JwtAuthenticationFilter(JwtDecoder jwtDecoder) {
		this.jwtDecoder = jwtDecoder;
	}

	@Override
	protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
		return "POST".equalsIgnoreCase(request.getMethod()) && "/auth/login".equals(request.getServletPath());
	}

	@Override
	protected void doFilterInternal(
			@NonNull HttpServletRequest request,
			@NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain) throws ServletException, java.io.IOException {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header == null || header.isBlank()) {
			filterChain.doFilter(request, response);
			return;
		}
		if (!header.regionMatches(true, 0, "Bearer ", 0, 7)) {
			writeUnauthorized(response);
			return;
		}
		String token = header.substring(7).trim();
		if (token.isEmpty()) {
			writeUnauthorized(response);
			return;
		}
		try {
			Jwt jwt = jwtDecoder.decode(token);
			String role = jwt.getClaimAsString(JwtService.ROLE_CLAIM);
			if (role == null || role.isBlank()) {
				role = JwtService.USER_ROLE;
			}
			var authentication = new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority(role)));
			SecurityContextHolder.getContext().setAuthentication(authentication);
			filterChain.doFilter(request, response);
		}
		catch (JwtException ex) {
			SecurityContextHolder.clearContext();
			writeUnauthorized(response);
		}
	}

	private static void writeUnauthorized(HttpServletResponse response) throws java.io.IOException {
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.getWriter().write("{\"error\":\"Недействительный или просроченный токен\"}");
	}

}
