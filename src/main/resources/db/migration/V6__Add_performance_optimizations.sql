-- flyway:transactional=false

-- ===========================================
-- Add materialized view for daily energy consumption
-- ===========================================
CREATE MATERIALIZED VIEW IF NOT EXISTS daily_energy_consumption AS
SELECT
    home_id,
    consumption_date,
    total_consumption,
    created_at
FROM energy_consumption
WHERE consumption_date >= CURRENT_DATE - INTERVAL '30 days';

-- Create index on materialized view
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes WHERE indexname = 'idx_daily_energy_home_date'
    ) THEN
        CREATE UNIQUE INDEX idx_daily_energy_home_date
        ON daily_energy_consumption(home_id, consumption_date);
    END IF;
END$$;

-- ===========================================
-- Add function to refresh materialized view
-- ===========================================
CREATE OR REPLACE FUNCTION refresh_daily_energy_consumption()
RETURNS VOID AS $$
BEGIN
    -- Using non-concurrent refresh for Flyway compatibility
    REFRESH MATERIALIZED VIEW daily_energy_consumption;
END;
$$ LANGUAGE plpgsql;

-- ===========================================
-- Create device status summary view
-- ===========================================
CREATE OR REPLACE VIEW device_status_summary AS
SELECT
    home_id,
    status,
    COUNT(*) AS device_count
FROM devices
GROUP BY home_id, status;

-- ===========================================
-- Create user home summary view
-- ===========================================
CREATE OR REPLACE VIEW user_home_summary AS
SELECT
    u.id AS user_id,
    u.username,
    COUNT(DISTINCT h.id) AS home_count,
    COUNT(DISTINCT d.id) AS device_count,
    COUNT(DISTINCT r.id) AS room_count
FROM users u
LEFT JOIN homes h ON u.id = h.user_id
LEFT JOIN rooms r ON h.id = r.home_id
LEFT JOIN devices d ON h.id = d.home_id
GROUP BY u.id, u.username;

-- ===========================================
-- Add indexes for better query performance
-- (Non-concurrent for Flyway compatibility)
-- ===========================================
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes WHERE indexname = 'idx_device_metrics_type_value'
    ) THEN
        CREATE INDEX idx_device_metrics_type_value ON device_metrics(metric_type, metric_value);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes WHERE indexname = 'idx_automation_rules_enabled'
    ) THEN
        CREATE INDEX idx_automation_rules_enabled ON automation_rules(enabled);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes WHERE indexname = 'idx_notifications_type_created'
    ) THEN
        CREATE INDEX idx_notifications_type_created ON notifications(type, created_at);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes WHERE indexname = 'idx_device_events_type_created'
    ) THEN
        CREATE INDEX idx_device_events_type_created ON device_events(event_type, created_at);
    END IF;
END$$;

-- ===========================================
-- Add function to get home statistics
-- ===========================================
CREATE OR REPLACE FUNCTION get_home_statistics(home_id BIGINT)
RETURNS TABLE(
    total_devices BIGINT,
    online_devices BIGINT,
    total_power_consumption DOUBLE PRECISION,
    daily_energy_usage DOUBLE PRECISION
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        COUNT(d.id)::BIGINT AS total_devices,
        COUNT(CASE WHEN d.status = 'ONLINE' THEN 1 END)::BIGINT AS online_devices,
        COALESCE(SUM(d.power_consumption), 0) AS total_power_consumption,
        COALESCE((
            SELECT ec.total_consumption
            FROM energy_consumption ec
            WHERE ec.home_id = $1
              AND ec.consumption_date = CURRENT_DATE
        ), 0) AS daily_energy_usage
    FROM devices d
    WHERE d.home_id = $1;
END;
$$ LANGUAGE plpgsql;
