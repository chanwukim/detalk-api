INSERT INTO pricing_plan (name)
SELECT unnest(ARRAY['Free', 'Paid', 'Paid with free trial or plan'])
EXCEPT
SELECT name FROM pricing_plan;