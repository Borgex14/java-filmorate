package ru.yandex.practicum.filmorate.storage.like;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public final class LikeDbStorage implements LikeStorage {

    private final JdbcTemplate jdbcTemplate;
    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public Long getLikesById(Long id) {
        final String filmLikesQuery = "SELECT COUNT(*) FROM likes WHERE film_id = ?";
        return jdbcTemplate.queryForObject(filmLikesQuery, Long.class, id);
    }

    @Override
    public Map getListOfLikesById(List<Long> filmIds) {
        final String filmLikesQuery = "SELECT film_id, COUNT(*) AS like_count FROM likes WHERE film_id IN (:filmIds) GROUP BY film_id";

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("filmIds", filmIds);

        List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(filmLikesQuery, parameters);

        Map<Long, Long> likesMap = new HashMap<>();
        for (Map<String, Object> row : results) {
            Long filmId = ((Number) row.get("film_id")).longValue();
            Long likeCount = ((Number) row.get("like_count")).longValue();
            likesMap.put(filmId, likeCount);
        }

        return likesMap;
    }
}
