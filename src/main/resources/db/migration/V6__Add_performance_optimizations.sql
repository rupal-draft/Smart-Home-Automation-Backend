-- Add materialized view for daily energy consumption
CREATE MATERIALIZED VIEW daily_energy_consumption AS
SELECT
    home_id,
    consumption_date,
    total_consumption,
    created_at
FROM energy_consumption
WHERE consumption_date >= CURRENT_DATE - INTERVAL '30 days';

-- Create index on materialized view
CREATE UNIQUE INDEX idx_daily_energy_home_date ON daily_energy_consumption(home_id, consumption_date);

-- Add function to refresh materialized view
CREATE OR REPLACE FUNCTION refresh_daily_energy_consumption()
RETURNS VOID AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY daily_energy_consumption;
END;
$$ LANGUAGE plpgsql;

-- Create device status summary view
CREATE VIEW device_status_summary AS
SELECT
    home_id,
    status,
    COUNT(*) as device_count
FROM devices
GROUP BY home_id, status;

-- Create user home summary view
CREATE VIEW user_home_summary AS
SELECT
    u.id as user_id,
    u.username,
    COUNT(DISTINCT h.id) as home_count,
    COUNT(DISTINCT d.id) as device_count,
    COUNT(DISTINCT r.id) as room_count
FROM users u
LEFT JOIN homes h ON u.id = h.user_id
LEFT JOIN rooms r ON h.id = r.home_id
LEFT JOIN devices d ON h.id = d.home_id
GROUP BY u.id, u.username;

-- Add indexes for better query performance
CREATE INDEX CONCURRENTLY idx_device_metrics_type_value ON device_metrics(metric_type, metric_value);
CREATE INDEX CONCURRENTLY idx_automation_rules_enabled ON automation_rules(enabled);
CREATE INDEX CONCURRENTLY idx_notifications_type_created ON notifications(type, created_at);
CREATE INDEX CONCURRENTLY idx_device_events_type_created ON device_events(event_type, created_at);

-- Add function to get home statistics
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
        COUNT(d.id)::BIGINT as total_devices,
        COUNT(CASE WHEN d.status = 'ONLINE' THEN 1 END)::BIGINT as online_devices,
        COALESCE(SUM(d.power_consumption), 0) as total_power_consumption,
        COALESCE((
            SELECT ec.total_consumption
            FROM energy_consumption ec
            WHERE ec.home_id = $1
            AND ec.consumption_date = CURRENT_DATE
        ), 0) as daily_energy_usage
    FROM devices d
    WHERE d.home_id = $1;
END;
$$ LANGUAGE plpgsql;