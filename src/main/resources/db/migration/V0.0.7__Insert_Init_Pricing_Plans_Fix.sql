INSERT INTO pricing_plan (name)
SELECT t.name
FROM (
    VALUES
        ('Free'),
        ('Paid'),
        ('Paid with free trial or plan')
) AS t(name)
WHERE NOT EXISTS (
    SELECT 1
    FROM pricing_plan p
    WHERE p.name = t.name
);