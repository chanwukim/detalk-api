INSERT INTO pricing_plan (name)
SELECT t.name
FROM (
    SELECT 'Free' AS name
    UNION ALL
    SELECT 'Paid'
    UNION ALL
    SELECT 'Paid with free trial or plan'
) t
WHERE t.name NOT IN (
    SELECT name
    FROM pricing_plan
);