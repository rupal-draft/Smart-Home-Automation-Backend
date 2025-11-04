-- Notifications table
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    priority VARCHAR(20) DEFAULT 'MEDIUM',
    read BOOLEAN DEFAULT FALSE,
    related_entity_type VARCHAR(50),
    related_entity_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- User notification preferences
CREATE TABLE user_notification_preferences (
    user_id BIGINT NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    email_notifications BOOLEAN DEFAULT FALSE,
    push_notifications BOOLEAN DEFAULT TRUE,
    PRIMARY KEY (user_id, notification_type),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes for notifications
CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_read ON notifications(read);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);

-- Insert default notification preferences
INSERT INTO user_notification_preferences (user_id, notification_type, enabled, email_notifications, push_notifications) VALUES
(1, 'DEVICE_STATUS', true, true, true),
(1, 'SECURITY_ALERT', true, true, true),
(1, 'ENERGY_USAGE', true, false, true),
(1, 'AUTOMATION_TRIGGERED', true, false, true),
(1, 'SYSTEM_MAINTENANCE', true, true, true),
(2, 'DEVICE_STATUS', true, true, true),
(2, 'SECURITY_ALERT', true, true, true),
(2, 'ENERGY_USAGE', true, false, true),
(2, 'AUTOMATION_TRIGGERED', true, false, true),
(2, 'SYSTEM_MAINTENANCE', true, true, true);

-- Insert sample notifications
INSERT INTO notifications (user_id, title, message, type, priority, related_entity_type, related_entity_id) VALUES
(2, 'New Device Added', 'Smart TV was successfully added to your Living Room', 'DEVICE_STATUS', 'LOW', 'DEVICE', 2),
(2, 'High Energy Usage', 'Unusual energy consumption detected in Kitchen', 'ENERGY_USAGE', 'HIGH', 'HOME', 1),
(2, 'Automation Triggered', 'Morning Wake Up automation was executed', 'AUTOMATION_TRIGGERED', 'LOW', 'AUTOMATION', 1);