package com.example.demo.util;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {


    public static final String SECRET = "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437";


  //    public String generateToken(UserDetails userDetails) {
  //   CustomUserDetails customUser = (CustomUserDetails) userDetails;

  //   Map<String, Object> claims = new HashMap<>();
  //   claims.put("userId", customUser.getId());
  //   claims.put("role", customUser.getRole());

  //   return createToken(claims, userDetails.getUsername());
  // }


   private String createToken(Map<String , Object> claims, String subject) {
    return Jwts.builder()
      .setClaims(claims)
      .setSubject(subject)
      .setIssuedAt(new Date(System.currentTimeMillis()))
      .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 hrs
      .signWith(getSignInKey(), SignatureAlgorithm.HS256)
      .compact();
  }

  private Key getSignInKey() {
    byte[] keyBytes = Decoders.BASE64.decode(SECRET);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  public String extractUsername(String token){
    return extractClaim(token, Claims::getSubject);
  }

  public Date extractExpiration(String token){
    return extractClaim(token, Claims::getExpiration);
  }

  public <T> T extractClaim(String token , Function<Claims , T> claimsResolver){
    Claims claims = Jwts.parserBuilder()
    .setSigningKey(getSignInKey())
    .build()
    .parseClaimsJws(token)
    .getBody();
    return claimsResolver.apply(claims);
  }


  private Boolean isTokenExpired(String token){
    return extractExpiration(token).before(new Date());
  }

  public Boolean validateToken(String token) {
    try {
      return !isTokenExpired(token);
    } catch (Exception e) {
      return false;
    }
  }

  public String extractRole(String token) {
    return extractClaim(token, claims -> claims.get("role").toString());
}
    public Integer extractUserId(String token) {
        return (Integer) extractClaim(token , claims->claims.get("userId"));
    }
}