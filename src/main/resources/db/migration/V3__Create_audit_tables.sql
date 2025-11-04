-- Audit table for user actions
CREATE TABLE user_audit (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    resource_id BIGINT,
    old_values JSONB,
    new_values JSONB,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Device state history table
CREATE TABLE device_state_history (
    id BIGSERIAL PRIMARY KEY,
    device_id BIGINT NOT NULL,
    old_status VARCHAR(20),
    new_status VARCHAR(20) NOT NULL,
    changed_by BIGINT,
    reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE CASCADE,
    FOREIGN KEY (changed_by) REFERENCES users(id) ON DELETE SET NULL
);

-- Create indexes for audit tables
CREATE INDEX idx_user_audit_user_id ON user_audit(user_id);
CREATE INDEX idx_user_audit_created_at ON user_audit(created_at);
CREATE INDEX idx_device_state_history_device_id ON device_state_history(device_id);
CREATE INDEX idx_device_state_history_created_at ON device_state_history(created_at);

-- Insert initial device state history
INSERT INTO device_state_history (device_id, old_status, new_status, changed_by, reason)
SELECT id, 'OFFLINE', status, 1, 'Initial device setup'
FROM devices;