package ru.yandex.practicum.filmorate.storage.like;

import java.util.List;
import java.util.Map;

public interface LikeStorage {

    Long getLikesById(Long id);

    Map<Long, Long> getListOfLikesById(List<Long> filmIds);
}
