package com.tiketing.api.reservation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tiketing.api.reservation.facade.SeatFacade;
import com.tiketing.api.reservation.service.SeatService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/seats")
public class SeatController {
	
	private final SeatService seatService;
	private final SeatFacade seatFacade;
	
	@Operation(summary = "좌석 5분 임시 선점", description = "결제를 위해 5분간 좌석에 락(Lock)을 겁니다.")
	@PostMapping("/{seatId}/reserve")
	public ResponseEntity<Void> reserveSeat(
			@PathVariable("seatId") Long seatId,
			@RequestParam("userId") Long userId // 임시로 파라미터로 받게끔 처리
	) {
		seatFacade.reserveSeat(seatId, userId);
		return ResponseEntity.ok().build();
	}
	
	@Operation(summary = "좌석 선점 취소 (락 해제)", description = "결제를 취소하고 선점한 좌석을 즉시 해제합니다.")
	@DeleteMapping("/{seatId}/reserve")
	public ResponseEntity<Void> unlockSeat(
			@PathVariable("seatId") Long seatId,
			@RequestParam("userId") Long userId // 임시로 파라미터로 받게끔 처리
	) {
		seatService.unlockSeat(seatId, userId);
		return ResponseEntity.ok().build();
	}
}
