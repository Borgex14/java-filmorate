CREATE TABLE IF NOT EXISTS users (
  user_id INTEGER PRIMARY KEY,
  email VARCHAR(255),
  login VARCHAR(255),
  name VARCHAR(255),
  birthday DATE
);