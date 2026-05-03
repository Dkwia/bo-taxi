package com.bootaxi.trip.security;

import com.bootaxi.contracts.security.JwtClaims;
import com.bootaxi.contracts.security.JwtSupport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final String secret;
    private final String issuer;

    public JwtService(@Value("${app.security.jwt-secret}") String secret,
                      @Value("${app.security.jwt-issuer:bo-taxi}") String issuer) {
        this.secret = secret;
        this.issuer = issuer;
    }

    public JwtClaims parse(String token) {
        return JwtSupport.verify(token, secret, issuer);
    }
}
