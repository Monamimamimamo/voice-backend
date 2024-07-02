package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.FriendshipOffer;
import org.example.service.AuthService;
import org.example.service.FriendshipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/friendship")
@Slf4j
public class FriendshipController {

    private final FriendshipService friendshipService;
    private final AuthService authService;

    @CrossOrigin("*")
    @GetMapping("/history/{type}")
    public ResponseEntity<List<FriendshipOffer>> getOffers(
            @PathVariable String type,
            HttpServletRequest request,
            @RequestParam String belonging) {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null ||!authHeader.startsWith("Bearer ")) {
            log.error("Unauthorized request");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        String userName = authService.getNameFromAuthToken(authHeader);
        friendshipService.validateType(type);
        List<FriendshipOffer> offers = friendshipService.getOffersByTypeAndBelonging(userName, type, belonging);
        return ResponseEntity.ok(offers);
    }
}
