package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.service.JwtService;
import org.example.domain.FriendshipOffer;
import org.example.service.FriendshipService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@AllArgsConstructor
@RequestMapping("/friendship")
@Slf4j
public class FriendshipController {

    private final FriendshipService friendshipService;
    private final JwtService jwtService;

    @Scheduled(cron = "0 0 0 * * ?")
    public void deleteOldFriendshipOffers() {
        LocalDateTime timeAgo = LocalDateTime.now().minusMonths(1);
        friendshipService.deleteByTimestampBefore(timeAgo);
    }

    @PostMapping("/remove/{friend}")
    @Operation(summary = "Удаление пользователя из списка друзей", parameters = {@Parameter(in = ParameterIn.HEADER, name = HttpHeaders.AUTHORIZATION, required = true, description = "JWT Bearer токен для хэдэра авторизации")})
    public ResponseEntity<String> removeFromFriends(@Parameter(description = "Логин удаляемого пользователя") @PathVariable String friend,
                                                    HttpServletRequest request) throws ExecutionException, InterruptedException
    {
        String userName = jwtService.extractUserName(request);
        return ResponseEntity.ok(friendshipService.deleteFromFriends("remove", userName, friend));
    }


    @GetMapping("/history/{type}")
    @Operation(summary = "Получение истории предложений дружбы", description = "При отправке запросе accepted или refused предложений ОНИ БУДУТ СТИРАТЬСЯ ИЗ БД \n\t это нужно чтобы бд не засорялось, пользователь один раз чекает принятые и отклонённые приглосы и их история стирается", parameters = {@Parameter(in = ParameterIn.HEADER, name = HttpHeaders.AUTHORIZATION, required = true, description = "JWT Bearer токен для хэдэра авторизации"),})
    public ResponseEntity<List<FriendshipOffer>> getOffers(
            @Parameter(description = "Тип предложения дружбы: \n\t- pending (ожидающие предложения) \n\t- accepted (подтверждённые предложения) \n\t- refused (отклонённые предложения)", schema = @Schema(implementation = String.class, allowableValues = {"pending", "accepted", "refused"}))
            @PathVariable String type,
            @Parameter(description = "Кем является пользователь \n\t- sender (отправитель) \n\t- sender (получатель)", schema = @Schema(implementation = String.class, allowableValues = {"sender", "receiver"}))
            @RequestParam String belonging,
            HttpServletRequest request)
    {
        String userName = jwtService.extractUserName(request);
        List<FriendshipOffer> offers = friendshipService.getOffersByTypeAndBelonging(userName, type, belonging);
        return ResponseEntity.ok(offers);
    }
}

