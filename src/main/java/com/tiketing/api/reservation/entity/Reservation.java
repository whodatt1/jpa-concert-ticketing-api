package com.tiketing.api.reservation.entity;

import com.tiketing.api.reservation.enums.ReservationStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reservation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long reservationId;

    @Column(name = "user_id", nullable = false)
    private Long userId; // 예매한 유저
    
    // 단방향
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @Enumerated(EnumType.STRING)
    @Column(name = "reservation_status", nullable = false)
    private ReservationStatus status; // PENDING(결제대기), COMPLETED(예매완료), CANCELED(취소됨)

    @Builder
    public Reservation(Long userId, Seat seat) {
        this.userId = userId;
        this.seat = seat;
        this.status = ReservationStatus.PENDING; // 초기 상태는 무조건 PENDING으로 강제
    }

    // 예매 취소
    public void cancel() {
        this.status = ReservationStatus.CANCELED;
    }
    
    // 결제 완료
    public void complete() {
        this.status = ReservationStatus.COMPLETED;
    }
}
