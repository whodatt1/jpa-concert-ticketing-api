package com.tiketing.api.payment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tiketing.api.payment.service.PaymentService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {
	
	private final PaymentService paymentService;
	
	@Operation(summary = "결제 승인 및 좌석 확정", description = "결제를 완료하고 좌석을 최종적으로 내 것으로 확정(SOLD)합니다.")
    @PostMapping("/confirm")
    public ResponseEntity<String> confirmPayment(
            @RequestParam("seatId") Long seatId,
            @RequestParam("userId") Long userId
    ) {
        paymentService.confirmPayment(seatId, userId);
        return ResponseEntity.ok("결제가 성공적으로 완료되었으며 좌석 예매가 확정되었습니다.");
    }
}
