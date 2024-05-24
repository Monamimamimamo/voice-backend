package org.example.controller;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import org.example.domain.FriendshipOffer;
import org.example.service.FriendshipService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/friendship")
public class FriendshipController {
    private final FriendshipService friendshipService;

    public FriendshipController(FriendshipService friendshipService) {
        this.friendshipService = friendshipService;
    }


    @GetMapping("/accepted")
    public ResponseEntity<List<FriendshipOffer>> getAcceptedOffers(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null ||!authHeader.startsWith("Bearer ")) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // Unauthorized access
        }
        String token = authHeader.substring(7);
        Claims claims = Jwts.parser()
                .setSigningKey("FBF0E5C5-056A-4DE9-A9B4-CAC04513C5D8".getBytes())
                .parseClaimsJws(token)
                .getBody();
        String userName = claims.get("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress", String.class);

        List<FriendshipOffer> acceptedOffers = friendshipService.getAcceptedOffers(userName);
        return ResponseEntity.ok(acceptedOffers);
    }



    @GetMapping("/pending")
    public ResponseEntity<List<FriendshipOffer>> getPendingOffers(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null ||!authHeader.startsWith("Bearer ")) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // Unauthorized access
        }
        String token = authHeader.substring(7);
        Claims claims = Jwts.parser()
                .setSigningKey("FBF0E5C5-056A-4DE9-A9B4-CAC04513C5D8".getBytes())
                .parseClaimsJws(token)
                .getBody();
        String userName = claims.get("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress", String.class);
        List<FriendshipOffer> acceptedOffers = friendshipService.getPendingOffers(userName);
        return ResponseEntity.ok(acceptedOffers);
    }

    @GetMapping("/refused")
    public ResponseEntity<List<FriendshipOffer>> getRefusedOffers(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null ||!authHeader.startsWith("Bearer ")) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // Unauthorized access
        }
        String token = authHeader.substring(7);
        Claims claims = Jwts.parser()
                .setSigningKey("FBF0E5C5-056A-4DE9-A9B4-CAC04513C5D8".getBytes())
                .parseClaimsJws(token)
                .getBody();
        String userName = claims.get("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress", String.class);
        List<FriendshipOffer> acceptedOffers = friendshipService.getRefusedOffers(userName);
        return ResponseEntity.ok(acceptedOffers);
    }
}
