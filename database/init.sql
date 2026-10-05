-- ========================================================
-- AK BANK — Digital Banking Demo Database Schema & Seed Data
-- ========================================================

CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    cnic VARCHAR(50),
    address TEXT,
    city VARCHAR(100),
    role VARCHAR(50) DEFAULT 'CUSTOMER',
    transaction_pin VARCHAR(10) DEFAULT '1234',
    is_biometric_enabled BOOLEAN DEFAULT TRUE,
    is_suspended BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS accounts (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    account_title VARCHAR(255) NOT NULL,
    account_number VARCHAR(50) UNIQUE NOT NULL,
    iban VARCHAR(50) UNIQUE NOT NULL,
    account_type VARCHAR(50) NOT NULL,
    balance NUMERIC(15, 2) DEFAULT 0.00,
    available_balance NUMERIC(15, 2) DEFAULT 0.00,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    opening_date DATE DEFAULT CURRENT_DATE
);

CREATE TABLE IF NOT EXISTS transactions (
    id SERIAL PRIMARY KEY,
    reference_number VARCHAR(100) UNIQUE NOT NULL,
    sender_account_id INTEGER REFERENCES accounts(id),
    receiver_account_id INTEGER,
    sender_name VARCHAR(255),
    receiver_name VARCHAR(255),
    bank_name VARCHAR(100),
    amount NUMERIC(15, 2) NOT NULL,
    fee NUMERIC(15, 2) DEFAULT 0.00,
    type VARCHAR(50) NOT NULL,
    category VARCHAR(50) NOT NULL,
    status VARCHAR(50) DEFAULT 'COMPLETED',
    note TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS beneficiaries (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    nickname VARCHAR(100),
    bank_name VARCHAR(100) NOT NULL,
    account_number VARCHAR(50) NOT NULL,
    iban VARCHAR(50),
    is_favorite BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS cards (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    account_id INTEGER REFERENCES accounts(id),
    card_number VARCHAR(50) NOT NULL,
    masked_number VARCHAR(50) NOT NULL,
    card_holder_name VARCHAR(255) NOT NULL,
    expiry_month INTEGER NOT NULL,
    expiry_year INTEGER NOT NULL,
    cvv VARCHAR(10) NOT NULL,
    card_type VARCHAR(50) DEFAULT 'DEBIT_VISA',
    is_frozen BOOLEAN DEFAULT FALSE,
    is_online_enabled BOOLEAN DEFAULT TRUE,
    is_intl_enabled BOOLEAN DEFAULT FALSE,
    spending_limit NUMERIC(15, 2) DEFAULT 150000.00,
    current_spent NUMERIC(15, 2) DEFAULT 0.00,
    card_pin VARCHAR(10) DEFAULT '1234'
);

CREATE TABLE IF NOT EXISTS billers (
    id SERIAL PRIMARY KEY,
    category VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS bills (
    id SERIAL PRIMARY KEY,
    biller_id INTEGER REFERENCES billers(id),
    consumer_number VARCHAR(100) NOT NULL,
    title VARCHAR(255) NOT NULL,
    amount NUMERIC(15, 2) NOT NULL,
    due_date DATE NOT NULL,
    is_paid BOOLEAN DEFAULT FALSE,
    paid_date TIMESTAMP,
    reference_number VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS notifications (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) DEFAULT 'TRANSACTION',
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS support_tickets (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    customer_name VARCHAR(255) NOT NULL,
    customer_email VARCHAR(255) NOT NULL,
    category VARCHAR(50) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    status VARCHAR(50) DEFAULT 'OPEN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id SERIAL PRIMARY KEY,
    user_id INTEGER,
    user_email VARCHAR(255),
    action VARCHAR(100) NOT NULL,
    resource VARCHAR(255),
    result VARCHAR(50) DEFAULT 'SUCCESS',
    ip_address VARCHAR(50) DEFAULT '192.168.1.1',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- SEED DEMO USERS
INSERT INTO users (email, password_hash, full_name, phone, cnic, address, city, role, transaction_pin)
VALUES 
('demo@akbank.demo', '$2b$12$e8x/yqK4w4Z4.exampleHashDemo', 'Muhammad Mawiya', '+92 300 1234567', '42101-5829143-7', 'PECHS Block 6', 'Karachi', 'CUSTOMER', '1234'),
('admin@akbank.demo', '$2b$12$e8x/yqK4w4Z4.exampleHashAdmin', 'AK Bank Admin', '+92 321 9876543', '61101-1122334-1', 'Blue Area', 'Islamabad', 'ADMIN', '9999'),
('support@akbank.demo', '$2b$12$e8x/yqK4w4Z4.exampleHashSupport', 'Ayesha Support', '+92 333 4455667', '35201-9988776-5', 'Gulberg III', 'Lahore', 'SUPPORT', '5555')
ON CONFLICT (email) DO NOTHING;

-- SEED DEMO ACCOUNTS
INSERT INTO accounts (user_id, account_title, account_number, iban, account_type, balance, available_balance)
VALUES 
(1, 'Muhammad Mawiya - Current', '001234567890', 'PK78AKBK0012345678901234', 'CURRENT', 145250.00, 145250.00),
(1, 'Muhammad Mawiya - Savings', '009876543210', 'PK21AKBK0098765432109876', 'SAVINGS', 380000.00, 380000.00)
ON CONFLICT (account_number) DO NOTHING;
