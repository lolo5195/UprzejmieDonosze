package com.projekt.uprzejmiedonosze.service;

import com.projekt.uprzejmiedonosze.dto.UserStats;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class StatisticsService {

    private static final String FIND_STATS_BY_USER_ID_SQL =
            "SELECT submitted_count, received_count FROM v_user_stats WHERE user_id = ?";

    private final JdbcTemplate jdbcTemplate;

    public StatisticsService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public UserStats getStatsForUser(Long userId) {
        List<UserStats> stats = jdbcTemplate.query(
                FIND_STATS_BY_USER_ID_SQL,
                (resultSet, rowNumber) -> new UserStats(
                        resultSet.getLong("submitted_count"),
                        resultSet.getLong("received_count")
                ),
                userId
        );

        return stats.isEmpty() ? new UserStats(0, 0) : stats.get(0);
    }
}
