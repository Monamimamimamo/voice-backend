package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.FriendshipOffer;
import org.example.domain.FriendshipOfferRepo;
import org.example.service.AuthService;
import org.example.service.FriendshipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
    private final AuthService authService;

    @Scheduled(cron = "0 0 0 * * ?")
    public void deleteOldFriendshipOffers() {
        LocalDateTime timeAgo = LocalDateTime.now().minusMonths(1);
        friendshipService.deleteByTimestampBefore(timeAgo);
    }

    @CrossOrigin("*")
    @PostMapping("/remove/{friend}")
    @Operation(summary = "Удаление пользователя из списка друзей",
            parameters = {@Parameter(in = ParameterIn.HEADER, name = HttpHeaders.AUTHORIZATION, required = true, description = "JWT Bearer токен для хэдэра авторизации")})
    public ResponseEntity<String> removeFromFriends(@Parameter (description = "Логин удаляемого пользователя") @PathVariable String friend,
                                                    HttpServletRequest request) throws ExecutionException, InterruptedException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null ||!authHeader.startsWith("Bearer ")) {
            log.error("Unauthorized request");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        String userName = authService.getNameFromAuthToken(authHeader);
        return ResponseEntity.ok(friendshipService.deleteFromFriends("remove" ,userName, friend));
    }


    @CrossOrigin("*")
    @GetMapping("/history/{type}")
    @Operation(summary = "Получение истории предложений дружбы", description = """
            При отправке запросе accepted или refused предложений ОНИ БУДУТ СТИРАТЬСЯ ИЗ БД ****
            это нужно чтобы бд не засорялось, пользователь один раз чекает принятые и отклонённые приглосы и их история стирается""",
            parameters = {
                    @Parameter(in = ParameterIn.HEADER, name = HttpHeaders.AUTHORIZATION, required = true, description = "JWT Bearer токен для хэдэра авторизации"),
            })
    public ResponseEntity<List<FriendshipOffer>> getOffers(
            @Parameter(description = """
                    Тип предложения дружбы:
                    \t- pending (ожидающие предложения)
                    \t- accepted (подтверждённые предложения)
                    \t- refused (отклонённые предложения)""",
                    schema = @Schema(implementation = String.class, allowableValues = {"pending", "accepted", "refused"}))
            @PathVariable String type,
            HttpServletRequest request,
            @Parameter(description = """
                    Кем является пользователь
                    \t- sender (отправитель)
                    \t- sender (получатель)""",
                    schema = @Schema(implementation = String.class, allowableValues = {"sender", "receiver"}))
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
