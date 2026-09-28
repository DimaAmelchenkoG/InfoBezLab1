package ru.itmo.is.lab1.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import ru.itmo.is.lab1.config.AppProperties;

@Service
public class JwtService {

	static final String ROLE_CLAIM = "role";
	static final String USER_ROLE = "ROLE_USER";

	private final JwtEncoder jwtEncoder;
	private final AppProperties properties;

	public JwtService(JwtEncoder jwtEncoder, AppProperties properties) {
		this.jwtEncoder = jwtEncoder;
		this.properties = properties;
	}

	public String generateToken(String username) {
		Instant issuedAt = Instant.now();
		Instant expiresAt = issuedAt.plus(properties.getJwt().getTtl());
		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer("secure-rest-api")
				.subject(username)
				.issuedAt(issuedAt)
				.expiresAt(expiresAt)
				.claim(ROLE_CLAIM, USER_ROLE)
				.build();
		return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
	}

	public long expiresInSeconds() {
		return properties.getJwt().getTtl().toSeconds();
	}

	public static SecretKey secretKey(String secret) {
		byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
		if (bytes.length < 32) {
			throw new IllegalStateException("JWT secret must be at least 32 bytes");
		}
		return new SecretKeySpec(bytes, "HmacSHA256");
	}

}
