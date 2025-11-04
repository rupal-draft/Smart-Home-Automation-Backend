-- Enable UUID extension (optional)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =====================
-- USERS TABLE
-- =====================
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    enabled BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Roles collection (user_roles)
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- =====================
-- HOMES TABLE
-- =====================
CREATE TABLE homes (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    address TEXT,
    timezone VARCHAR(50) DEFAULT 'UTC' NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- =====================
-- ROOMS TABLE
-- =====================
CREATE TABLE rooms (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    type VARCHAR(50) NOT NULL,
    home_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (home_id) REFERENCES homes(id) ON DELETE CASCADE
);

-- =====================
-- DEVICES TABLE
-- =====================
CREATE TABLE devices (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    device_id VARCHAR(100) UNIQUE,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(20) DEFAULT 'OFFLINE' NOT NULL,
    home_id BIGINT NOT NULL,
    room_id BIGINT,
    manufacturer VARCHAR(100),
    model VARCHAR(100),
    ip_address VARCHAR(45),
    power_consumption DOUBLE PRECISION DEFAULT 0.0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (home_id) REFERENCES homes(id) ON DELETE CASCADE,
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE SET NULL
);

-- =====================
-- DEVICE METRICS TABLE
-- =====================
CREATE TABLE device_metrics (
    id BIGSERIAL PRIMARY KEY,
    device_id BIGINT NOT NULL,
    metric_type VARCHAR(50) NOT NULL,
    metric_value DOUBLE PRECISION NOT NULL,
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE CASCADE
);

-- =====================
-- AUTOMATION RULES TABLE
-- =====================
CREATE TABLE automation_rules (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    trigger_type VARCHAR(50) NOT NULL,
    trigger_condition JSONB NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    action_config JSONB NOT NULL,
    enabled BOOLEAN DEFAULT TRUE NOT NULL,
    home_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (home_id) REFERENCES homes(id) ON DELETE CASCADE
);

-- =====================
-- DEVICE EVENTS TABLE
-- =====================
CREATE TABLE device_events (
    id BIGSERIAL PRIMARY KEY,
    device_id BIGINT NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    event_data JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE CASCADE
);

-- =====================
-- ENERGY CONSUMPTION TABLE
-- =====================
CREATE TABLE energy_consumption (
    id BIGSERIAL PRIMARY KEY,
    home_id BIGINT NOT NULL,
    total_consumption DOUBLE PRECISION NOT NULL,
    consumption_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(home_id, consumption_date),
    FOREIGN KEY (home_id) REFERENCES homes(id) ON DELETE CASCADE
);

-- =====================
-- INDEXES
-- =====================
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_homes_user_id ON homes(user_id);
CREATE INDEX idx_rooms_home_id ON rooms(home_id);
CREATE INDEX idx_devices_home_id ON devices(home_id);
CREATE INDEX idx_devices_room_id ON devices(room_id);
CREATE INDEX idx_devices_status ON devices(status);
CREATE INDEX idx_devices_type ON devices(type);
CREATE INDEX idx_device_metrics_device_id ON device_metrics(device_id);
CREATE INDEX idx_device_metrics_recorded_at ON device_metrics(recorded_at);
CREATE INDEX idx_automation_rules_home_id ON automation_rules(home_id);
CREATE INDEX idx_device_events_device_id ON device_events(device_id);
CREATE INDEX idx_device_events_created_at ON device_events(created_at);
CREATE INDEX idx_energy_consumption_home_date ON energy_consumption(home_id, consumption_date);

-- =====================
-- UPDATE TIMESTAMP TRIGGERS
-- =====================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_users_updated_at
BEFORE UPDATE ON users
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_homes_updated_at
BEFORE UPDATE ON homes
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_rooms_updated_at
BEFORE UPDATE ON rooms
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_devices_updated_at
BEFORE UPDATE ON devices
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_automation_rules_updated_at
BEFORE UPDATE ON automation_rules
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
