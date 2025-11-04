-- Device groups for bulk operations
CREATE TABLE device_groups (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    home_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (home_id) REFERENCES homes(id) ON DELETE CASCADE
);

-- Junction table for devices and groups
CREATE TABLE device_group_members (
    group_id BIGINT NOT NULL,
    device_id BIGINT NOT NULL,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (group_id, device_id),
    FOREIGN KEY (group_id) REFERENCES device_groups(id) ON DELETE CASCADE,
    FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE CASCADE
);

-- Scenes for complex automations
CREATE TABLE scenes (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    home_id BIGINT NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (home_id) REFERENCES homes(id) ON DELETE CASCADE
);

-- Scene actions
CREATE TABLE scene_actions (
    id BIGSERIAL PRIMARY KEY,
    scene_id BIGINT NOT NULL,
    device_id BIGINT NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    action_config JSONB NOT NULL,
    execution_order INTEGER DEFAULT 0,
    FOREIGN KEY (scene_id) REFERENCES scenes(id) ON DELETE CASCADE,
    FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE CASCADE
);

-- Create indexes for new tables
CREATE INDEX idx_device_groups_home_id ON device_groups(home_id);
CREATE INDEX idx_device_group_members_group_id ON device_group_members(group_id);
CREATE INDEX idx_device_group_members_device_id ON device_group_members(device_id);
CREATE INDEX idx_scenes_home_id ON scenes(home_id);
CREATE INDEX idx_scene_actions_scene_id ON scene_actions(scene_id);
CREATE INDEX idx_scene_actions_device_id ON scene_actions(device_id);

-- Insert sample device groups
INSERT INTO device_groups (name, description, home_id) VALUES
('All Lights', 'All lighting devices in the home', 1),
('Kitchen Appliances', 'All smart appliances in the kitchen', 1),
('Entertainment System', 'TV and media devices', 1);

-- Add devices to groups
INSERT INTO device_group_members (group_id, device_id) VALUES
(1, 1), (1, 4), (1, 9), (1, 10),  -- All lights
(2, 6), (2, 7), (2, 8),           -- Kitchen appliances
(3, 2);                            -- Entertainment system

-- Insert sample scenes
INSERT INTO scenes (name, description, home_id) VALUES
('Movie Night', 'Dim lights and prepare entertainment system', 1),
('Good Night', 'Turn off all lights and appliances', 1),
('Morning Routine', 'Prepare home for the day', 1);

-- Insert scene actions
INSERT INTO scene_actions (scene_id, device_id, action_type, action_config, execution_order) VALUES
-- Movie Night scene
(1, 1, 'SET_BRIGHTNESS', '{"brightness": 20}', 1),
(1, 4, 'SET_BRIGHTNESS', '{"brightness": 10}', 2),
(1, 2, 'TURN_ON', '{}', 3),

-- Good Night scene
(2, 1, 'TURN_OFF', '{}', 1),
(2, 4, 'TURN_OFF', '{}', 2),
(2, 10, 'TURN_OFF', '{}', 3),
(2, 6, 'SET_MODE', '{"mode": "ENERGY_SAVING"}', 4),

-- Morning Routine scene
(3, 1, 'SET_BRIGHTNESS', '{"brightness": 80}', 1),
(3, 8, 'TURN_ON', '{}', 2);

-- Add trigger for scene updated_at
CREATE TRIGGER update_scenes_updated_at BEFORE UPDATE ON scenes
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_device_groups_updated_at BEFORE UPDATE ON device_groups
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();