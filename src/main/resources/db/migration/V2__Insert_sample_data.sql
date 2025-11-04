-- Insert sample admin user (password: admin123)
INSERT INTO users (username, email, password, first_name, last_name, enabled) VALUES
('admin', 'admin@smarthome.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuWk6K1a.LVcK9CLxaVe4xggzVBR.F5i', 'System', 'Administrator', true);

-- Insert sample regular user (password: user123)
INSERT INTO users (username, email, password, first_name, last_name, enabled) VALUES
('john_doe', 'john.doe@example.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuWk6K1a.LVcK9CLxaVe4xggzVBR.F5i', 'John', 'Doe', true);

-- Assign roles
INSERT INTO user_roles (user_id, role) VALUES
(1, 'ROLE_ADMIN'),
(1, 'ROLE_USER'),
(2, 'ROLE_USER');

-- Insert sample homes
INSERT INTO homes (name, address, timezone, user_id) VALUES
('My Smart Home', '123 Main St, Smart City, SC 12345', 'America/New_York', 2),
('Vacation Home', '456 Beach Rd, Coastal Town, CT 67890', 'America/Los_Angeles', 2);

-- Insert sample rooms
INSERT INTO rooms (name, description, type, home_id) VALUES
('Living Room', 'Main living area with entertainment system', 'LIVING_ROOM', 1),
('Master Bedroom', 'Primary bedroom with smart lighting', 'MASTER_BEDROOM', 1),
('Kitchen', 'Smart kitchen with connected appliances', 'KITCHEN', 1),
('Guest Room', 'Room for visitors with basic smart features', 'GUEST_ROOM', 1),
('Patio', 'Outdoor patio area', 'PATIO', 1),
('Beach View Room', 'Room with ocean view', 'BEDROOM', 2);

-- Insert sample devices
INSERT INTO devices (name, device_id, type, status, home_id, room_id, manufacturer, model, power_consumption) VALUES
-- Living Room Devices
('Living Room Light', 'light-living-001', 'LIGHT', 'ONLINE', 1, 1, 'Philips', 'Hue Color', 8.5),
('Smart TV', 'tv-living-001', 'ENTERTAINMENT', 'ONLINE', 1, 1, 'Samsung', 'QLED 65"', 120.0),
('Temperature Sensor', 'sensor-temp-living-001', 'SENSOR', 'ONLINE', 1, 1, 'Xiaomi', 'Temp Sensor', 0.5),

-- Master Bedroom Devices
('Bedroom Light', 'light-bedroom-001', 'LIGHT', 'IDLE', 1, 2, 'Philips', 'Hue White', 7.5),
('Smart Blinds', 'blinds-bedroom-001', 'APPLIANCE', 'ONLINE', 1, 2, 'Lutron', 'Serena', 15.0),

-- Kitchen Devices
('Refrigerator', 'fridge-kitchen-001', 'APPLIANCE', 'ONLINE', 1, 3, 'LG', 'Smart InstaView', 150.0),
('Oven', 'oven-kitchen-001', 'APPLIANCE', 'OFFLINE', 1, 3, 'Samsung', 'Smart Oven', 2000.0),
('Coffee Maker', 'coffee-kitchen-001', 'APPLIANCE', 'ONLINE', 1, 3, 'Breville', 'Smart Brewer', 1200.0),

-- Guest Room Devices
('Guest Room Light', 'light-guest-001', 'LIGHT', 'OFFLINE', 1, 4, 'TP-Link', 'Kasa Smart', 9.0),

-- Patio Devices
('Outdoor Lights', 'light-patio-001', 'LIGHT', 'ONLINE', 1, 5, 'Ring', 'Smart Lighting', 12.0),

-- Vacation Home Devices
('Beach House Main Light', 'light-beach-001', 'LIGHT', 'ONLINE', 2, 6, 'Philips', 'Hue White', 8.0),
('Security Camera', 'camera-beach-001', 'SECURITY_CAMERA', 'ONLINE', 2, 6, 'Arlo', 'Pro 4', 5.5);

-- Insert sample automation rules
INSERT INTO automation_rules (name, description, trigger_type, trigger_condition, action_type, action_config, enabled, home_id) VALUES
('Morning Wake Up', 'Turn on lights gradually in the morning', 'TIME', '{"time": "07:00", "days": ["MON", "TUE", "WED", "THU", "FRI"]}', 'DEVICE_ACTION', '{"devices": ["light-bedroom-001"], "action": "TURN_ON", "brightness": 50}', true, 1),
('Away Mode', 'Turn off all lights when no one is home', 'PRESENCE', '{"presence": "AWAY"}', 'DEVICE_ACTION', '{"devices": ["all_lights"], "action": "TURN_OFF"}', true, 1),
('Energy Saving', 'Turn off appliances during peak hours', 'TIME', '{"time": "18:00", "duration": "PT4H"}', 'DEVICE_ACTION', '{"devices": ["oven-kitchen-001", "coffee-kitchen-001"], "action": "TURN_OFF"}', true, 1),
('Security Mode', 'Turn on outdoor lights at sunset', 'SUNSET', '{"offset": "PT0S"}', 'DEVICE_ACTION', '{"devices": ["light-patio-001"], "action": "TURN_ON"}', true, 1);

-- Insert sample device metrics
INSERT INTO device_metrics (device_id, metric_type, metric_value) VALUES
(1, 'POWER_CONSUMPTION', 8.2),
(1, 'POWER_CONSUMPTION', 8.5),
(1, 'POWER_CONSUMPTION', 8.1),
(2, 'POWER_CONSUMPTION', 115.5),
(2, 'POWER_CONSUMPTION', 120.0),
(2, 'POWER_CONSUMPTION', 118.7),
(6, 'POWER_CONSUMPTION', 145.0),
(6, 'POWER_CONSUMPTION', 150.0),
(6, 'POWER_CONSUMPTION', 148.2);

-- Insert sample energy consumption data
INSERT INTO energy_consumption (home_id, total_consumption, consumption_date) VALUES
(1, 12.5, CURRENT_DATE - INTERVAL '2 days'),
(1, 15.2, CURRENT_DATE - INTERVAL '1 day'),
(1, 11.8, CURRENT_DATE),
(2, 8.3, CURRENT_DATE - INTERVAL '1 day'),
(2, 9.1, CURRENT_DATE);