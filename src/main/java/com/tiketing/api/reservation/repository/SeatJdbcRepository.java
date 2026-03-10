package com.tiketing.api.reservation.repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SeatJdbcRepository {
	
	private final JdbcTemplate jdbcTemplate;
	
	public void batchInsertSeats(List<SeatBatchDto> seats) {
		
		String sql = "INSERT INTO seat (concert_schedule_id, seat_name, seat_rating, seat_price, seat_status, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, 'AVAILABLE', NOW(), NOW())";
		
		jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
			
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				SeatBatchDto seat = seats.get(i);
				ps.setLong(1, seat.scheduleId);
				ps.setString(2, seat.seatName);
				ps.setString(3, seat.seatRating);
				ps.setLong(4, seat.seatPrice);
			}
			
			@Override
			public int getBatchSize() {
				return seats.size();
			}
		});
	}
	
	public record SeatBatchDto(
			Long scheduleId,
			String seatName,
			String seatRating,
			Long seatPrice
	) {}
}
