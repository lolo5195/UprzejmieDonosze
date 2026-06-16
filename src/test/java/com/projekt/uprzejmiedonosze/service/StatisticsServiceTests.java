package com.projekt.uprzejmiedonosze.service;

import com.projekt.uprzejmiedonosze.dto.UserStats;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StatisticsServiceTests {

    private static final String FIND_STATS_BY_USER_ID_SQL =
            "SELECT submitted_count, received_count FROM v_user_stats WHERE user_id = ?";

    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    private final StatisticsService statisticsService = new StatisticsService(jdbcTemplate);

    @Test
    void getStatsForUserMapsJdbcRowToUserStats() throws Exception {
        when(jdbcTemplate.query(eq(FIND_STATS_BY_USER_ID_SQL), anyUserStatsRowMapper(), eq(7L)))
                .thenAnswer(invocation -> {
                    RowMapper<UserStats> rowMapper = invocation.getArgument(1);
                    ResultSet resultSet = mock(ResultSet.class);
                    when(resultSet.getLong("submitted_count")).thenReturn(3L);
                    when(resultSet.getLong("received_count")).thenReturn(2L);

                    return List.of(rowMapper.mapRow(resultSet, 0));
                });

        UserStats stats = statisticsService.getStatsForUser(7L);

        assertThat(stats.submittedCount()).isEqualTo(3);
        assertThat(stats.receivedCount()).isEqualTo(2);
        verify(jdbcTemplate).query(eq(FIND_STATS_BY_USER_ID_SQL), anyUserStatsRowMapper(), eq(7L));
    }

    @Test
    void getStatsForUserReturnsZeroStatsWhenUserDoesNotExist() {
        when(jdbcTemplate.query(eq(FIND_STATS_BY_USER_ID_SQL), anyUserStatsRowMapper(), eq(99L)))
                .thenReturn(List.of());

        UserStats stats = statisticsService.getStatsForUser(99L);

        assertThat(stats.submittedCount()).isZero();
        assertThat(stats.receivedCount()).isZero();
        verify(jdbcTemplate).query(eq(FIND_STATS_BY_USER_ID_SQL), anyUserStatsRowMapper(), eq(99L));
    }

    @SuppressWarnings("unchecked")
    private RowMapper<UserStats> anyUserStatsRowMapper() {
        return (RowMapper<UserStats>) any(RowMapper.class);
    }
}
