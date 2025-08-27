-- OWNERS
INSERT INTO owner (id, name, email) VALUES (1, 'Ana Pop', 'ana.pop@example.com');
INSERT INTO owner (id, name, email) VALUES (2, 'Bogdan Ionescu', 'bogdan.ionescu@example.com');

-- CARS
INSERT INTO car (id, vin, make, model, year_of_manufacture, owner_id)
VALUES (1, 'VIN12345', 'Dacia', 'Logan', 2018, 1);
INSERT INTO car (id, vin, make, model, year_of_manufacture, owner_id)
VALUES (2, 'VIN67890', 'VW', 'Golf', 2021, 2);

-- POLICIES (end_date obligatoriu + expiry_logged)
INSERT INTO insurancepolicy (id, car_id, provider, start_date, end_date, expiry_logged)
VALUES (1, 1, 'Allianz',  DATE '2024-01-01', DATE '2024-12-31', FALSE);

INSERT INTO insurancepolicy (id, car_id, provider, start_date, end_date, expiry_logged)
VALUES (2, 1, 'Groupama', DATE '2025-01-01', DATE '2025-12-31', FALSE);

INSERT INTO insurancepolicy (id, car_id, provider, start_date, end_date, expiry_logged)
VALUES (3, 2, 'Allianz',  DATE '2025-03-01', DATE '2025-09-30', FALSE);

-- CLAIMS
INSERT INTO insuranceclaim (id, car_id, claim_date, description, amount)
VALUES (1, 1, DATE '2025-06-15', 'Rear bumper damage', 950.00);

-- (opțional) una expirată ieri pentru scheduler
-- INSERT INTO insurancepolicy (id, car_id, provider, start_date, end_date, expiry_logged)
-- VALUES (4, 2, 'TestCo', DATE '2025-01-01', DATEADD('DAY', -1, CURRENT_DATE), FALSE);
