-- Qualimed Pharmacy — SQLite schema (reference).
-- Tables are also created automatically on first run by DBConnection.
--
-- The database file is set in db.properties (default: qualimed_pharmacy.db in project folder).

CREATE TABLE IF NOT EXISTS customer (
  customer_id INTEGER PRIMARY KEY AUTOINCREMENT,
  full_name TEXT NOT NULL,
  email TEXT NOT NULL UNIQUE,
  password_hash TEXT NOT NULL,
  contact_number TEXT,
  address TEXT,
  role TEXT NOT NULL DEFAULT 'customer',
  account_status TEXT NOT NULL DEFAULT 'active'
);

CREATE TABLE IF NOT EXISTS product (
  product_id INTEGER PRIMARY KEY AUTOINCREMENT,
  product_name TEXT NOT NULL,
  description TEXT,
  category TEXT,
  price REAL NOT NULL DEFAULT 0,
  stock_quantity INTEGER NOT NULL DEFAULT 0,
  expirydate TEXT,
  image_path TEXT
);

CREATE TABLE IF NOT EXISTS "order" (
  order_id INTEGER PRIMARY KEY AUTOINCREMENT,
  customer_id INTEGER NOT NULL,
  order_date TEXT NOT NULL DEFAULT (datetime('now')),
  total_amount REAL NOT NULL DEFAULT 0,
  status TEXT NOT NULL DEFAULT 'pending',
  FOREIGN KEY (customer_id) REFERENCES customer(customer_id)
);

CREATE TABLE IF NOT EXISTS order_item (
  order_item_id INTEGER PRIMARY KEY AUTOINCREMENT,
  order_id INTEGER NOT NULL,
  product_id INTEGER NOT NULL,
  quantity INTEGER NOT NULL,
  subtotal REAL NOT NULL,
  FOREIGN KEY (order_id) REFERENCES "order"(order_id) ON DELETE CASCADE,
  FOREIGN KEY (product_id) REFERENCES product(product_id)
);

CREATE TABLE IF NOT EXISTS payment (
  payment_id INTEGER PRIMARY KEY AUTOINCREMENT,
  order_id INTEGER NOT NULL,
  payment_method TEXT,
  payment_status TEXT,
  payment_date TEXT,
  FOREIGN KEY (order_id) REFERENCES "order"(order_id) ON DELETE CASCADE
);
