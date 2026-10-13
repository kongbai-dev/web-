CREATE DATABASE IF NOT EXISTS campus_market
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;
USE campus_market;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT NOT NULL AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  student_no VARCHAR(30),
  nickname VARCHAR(255),
  avatar VARCHAR(255),
  email VARCHAR(100),
  phone VARCHAR(255),
  campus VARCHAR(255),
  dorm_area VARCHAR(255),
  bio VARCHAR(255),
  role VARCHAR(32) NOT NULL,
  status VARCHAR(32) NOT NULL,
  created_at DATETIME(6),
  updated_at DATETIME(6),
  PRIMARY KEY (id),
  UNIQUE KEY uk_users_username (username),
  UNIQUE KEY uk_users_student_no (student_no),
  UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS email_verification_codes (
  email VARCHAR(100) NOT NULL,
  code_hash VARCHAR(255) NOT NULL,
  expires_at DATETIME(6) NOT NULL,
  last_sent_at DATETIME(6) NOT NULL,
  attempts INT NOT NULL DEFAULT 0,
  version BIGINT,
  PRIMARY KEY (email),
  KEY idx_email_codes_expires (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS categories (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL,
  sort_order INT,
  active BIT(1),
  created_at DATETIME(6),
  PRIMARY KEY (id),
  UNIQUE KEY uk_categories_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS products (
  id BIGINT NOT NULL AUTO_INCREMENT,
  seller_id BIGINT NOT NULL,
  category_id BIGINT NOT NULL,
  title VARCHAR(100) NOT NULL,
  description TEXT,
  original_price DECIMAL(38,2),
  price DECIMAL(38,2) NOT NULL,
  condition_level VARCHAR(255),
  trade_type VARCHAR(32),
  trade_location VARCHAR(255),
  status VARCHAR(32) NOT NULL,
  view_count INT,
  favorite_count INT,
  created_at DATETIME(6),
  updated_at DATETIME(6),
  PRIMARY KEY (id),
  KEY idx_product_status_created (status, created_at),
  KEY idx_product_category_status (category_id, status),
  KEY idx_product_seller_created (seller_id, created_at),
  CONSTRAINT fk_products_seller FOREIGN KEY (seller_id) REFERENCES users(id),
  CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS product_image_urls (
  product_id BIGINT NOT NULL,
  image_order INT NOT NULL,
  image_url VARCHAR(500),
  PRIMARY KEY (product_id, image_order),
  CONSTRAINT fk_product_images_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS favorites (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  created_at DATETIME(6),
  PRIMARY KEY (id),
  UNIQUE KEY uk_favorites_user_product (user_id, product_id),
  KEY idx_favorites_product (product_id),
  CONSTRAINT fk_favorites_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_favorites_product FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS browse_history (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  viewed_at DATETIME(6),
  PRIMARY KEY (id),
  UNIQUE KEY uk_history_user_product (user_id, product_id),
  KEY idx_history_product (product_id),
  CONSTRAINT fk_history_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_history_product FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS market_orders (
  id BIGINT NOT NULL AUTO_INCREMENT,
  order_no VARCHAR(255) NOT NULL,
  product_id BIGINT NOT NULL,
  buyer_id BIGINT NOT NULL,
  seller_id BIGINT NOT NULL,
  amount DECIMAL(38,2) NOT NULL,
  trade_location VARCHAR(255),
  buyer_note VARCHAR(255),
  status VARCHAR(32) NOT NULL,
  created_at DATETIME(6),
  confirmed_at DATETIME(6),
  paid_at DATETIME(6),
  delivered_at DATETIME(6),
  received_at DATETIME(6),
  completed_at DATETIME(6),
  cancelled_at DATETIME(6),
  PRIMARY KEY (id),
  UNIQUE KEY uk_market_orders_order_no (order_no),
  KEY idx_order_status (status),
  KEY idx_order_buyer_created (buyer_id, created_at),
  KEY idx_order_seller_created (seller_id, created_at),
  KEY idx_order_product (product_id),
  CONSTRAINT fk_orders_product FOREIGN KEY (product_id) REFERENCES products(id),
  CONSTRAINT fk_orders_buyer FOREIGN KEY (buyer_id) REFERENCES users(id),
  CONSTRAINT fk_orders_seller FOREIGN KEY (seller_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS reviews (
  id BIGINT NOT NULL AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  reviewer_id BIGINT NOT NULL,
  target_user_id BIGINT NOT NULL,
  rating INT,
  content VARCHAR(500),
  created_at DATETIME(6),
  PRIMARY KEY (id),
  UNIQUE KEY uk_reviews_order_reviewer (order_id, reviewer_id),
  KEY idx_reviews_target_user (target_user_id),
  CONSTRAINT fk_reviews_order FOREIGN KEY (order_id) REFERENCES market_orders(id),
  CONSTRAINT fk_reviews_reviewer FOREIGN KEY (reviewer_id) REFERENCES users(id),
  CONSTRAINT fk_reviews_target_user FOREIGN KEY (target_user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS reports (
  id BIGINT NOT NULL AUTO_INCREMENT,
  reporter_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  reason VARCHAR(255) NOT NULL,
  description VARCHAR(500),
  status VARCHAR(32),
  admin_id BIGINT,
  handle_result VARCHAR(500),
  created_at DATETIME(6),
  handled_at DATETIME(6),
  PRIMARY KEY (id),
  KEY idx_reports_status_created (status, created_at),
  KEY idx_reports_product (product_id),
  CONSTRAINT fk_reports_reporter FOREIGN KEY (reporter_id) REFERENCES users(id),
  CONSTRAINT fk_reports_product FOREIGN KEY (product_id) REFERENCES products(id),
  CONSTRAINT fk_reports_admin FOREIGN KEY (admin_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
