MERGE INTO genre AS target
USING (VALUES
    (1, 'Комедия'),
    (2, 'Драма'),
    (3, 'Мультфильм'),
    (4, 'Триллер'),
    (5, 'Документальный'),
    (6, 'Боевик')
) AS source (genre_id, genre_name)
ON target.id = source.genre_id
WHEN MATCHED THEN
    UPDATE SET name = source.genre_name
WHEN NOT MATCHED THEN
    INSERT (id, name) VALUES (source.genre_id, source.genre_name);

MERGE INTO rating AS target
USING (VALUES
    (1, 'G'),
    (2, 'PG'),
    (3, 'PG-13'),
    (4, 'R'),
    (5, 'NC-17')
) AS source (rating_id, rating_name)
ON target.id = source.rating_id
WHEN MATCHED THEN
    UPDATE SET name = source.rating_name
WHEN NOT MATCHED THEN
    INSERT (id, name) VALUES (source.rating_id, source.rating_name);