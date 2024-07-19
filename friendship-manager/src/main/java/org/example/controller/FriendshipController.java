package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.auth.JwtService;
import org.example.domain.OperationStatus;
import org.example.service.FriendshipService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
    @Operation(summary = "Удаление пользователя из списка друзей", parameters = {@Parameter(in = ParameterIn.HEADER, name = HttpHeaders.AUTHORIZATION, required = true, description = "JWT Bearer токен пользователя")})
    public ResponseEntity<String> removeFromFriends(@Parameter(description = "Логин удаляемого пользователя") @PathVariable String friend,
                                                    HttpServletRequest request) throws ExecutionException, InterruptedException {
        String userName = jwtService.extractUserName(request);
        if (userName == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(friendshipService.deleteFromFriends("remove", userName, friend));
    }


    @GetMapping("/history")
    @Operation(summary = "Получение истории предложений дружбы", description = "При отправке запроса accepted или refused предложений ОНИ БУДУТ СТИРАться ИЗ БД \n\t это нужно чтобы бд не засорялась, пользователь один раз чекает принятые и отклонённые приглосы и их история стирается", parameters = {@Parameter(in = ParameterIn.HEADER, name = HttpHeaders.AUTHORIZATION, required = true, description = "JWT Bearer токен для хэдэра авторизации"),})
    public ResponseEntity<List<Map<String, Object>>> getOffers(
            @Parameter(description = "Тип предложения дружбы: \n\t- pending (ожидающие предложения) \n\t- accepted (подтверждённые предложения) \n\t- refused (отклонённые предложения)", schema = @Schema(implementation = String.class, allowableValues = {"pending", "accepted", "refused"}))
            @RequestParam String type,
            @Parameter(description = "Кем является пользователь \n\t- sender (отправитель) \n\t- sender (получатель)", schema = @Schema(implementation = String.class, allowableValues = {"sender", "receiver"}))
            @RequestParam String belonging,
            HttpServletRequest request) {
        String userName = jwtService.extractUserName(request);

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = authHeader.substring(7);
        List<Map<String, Object>> offers = friendshipService.getOffersByTypeAndBelonging(userName, type, belonging, token);
        return ResponseEntity.ok(offers);
    }

    @PostMapping("/history/remove-pending-offer")
    @Operation(summary = "Удаление ожидающего запроса в друзья", parameters = {@Parameter(in = ParameterIn.HEADER, name = HttpHeaders.AUTHORIZATION, required = true, description = "JWT Bearer токен пользователя")})
    public ResponseEntity<OperationStatus> deletePendingOffer(
            HttpServletRequest request,
            @Parameter(description = "Тот, кому мы отправляли запрос") @RequestParam(name = "receiver", required = true) String receiver) {
        return friendshipService.deletePendingOffer(request, receiver);
    }
}

