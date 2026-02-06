-- =====================================================
-- Seed Sample Data for Testing
-- =====================================================

-- =====================================================
-- 1. Create Sample Users (password: Password123)
-- BCrypt hash for "Password123"
-- =====================================================

INSERT INTO users (id, email, password_hash, phone, first_name, last_name, role, email_verified, phone_verified)
VALUES
    -- Business Owners
    ('11111111-1111-1111-1111-111111111111', 'owner1@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqEBkEQdW3qtM5QzI.0T8bNvKhdOe', '+972501234567', 'David', 'Cohen', 'BUSINESS_OWNER', true, true),
    ('22222222-2222-2222-2222-222222222222', 'owner2@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqEBkEQdW3qtM5QzI.0T8bNvKhdOe', '+972502345678', 'Sarah', 'Levi', 'BUSINESS_OWNER', true, true),
    ('33333333-3333-3333-3333-333333333333', 'owner3@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqEBkEQdW3qtM5QzI.0T8bNvKhdOe', '+972503456789', 'Michael', 'Mizrahi', 'BUSINESS_OWNER', true, true),
    ('44444444-4444-4444-4444-444444444444', 'owner4@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqEBkEQdW3qtM5QzI.0T8bNvKhdOe', '+972504567890', 'Rachel', 'Shapira', 'BUSINESS_OWNER', true, true),
    -- Customers
    ('55555555-5555-5555-5555-555555555555', 'customer1@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqEBkEQdW3qtM5QzI.0T8bNvKhdOe', '+972505678901', 'Yossi', 'Ben-David', 'CUSTOMER', true, true),
    ('66666666-6666-6666-6666-666666666666', 'customer2@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqEBkEQdW3qtM5QzI.0T8bNvKhdOe', '+972506789012', 'Maya', 'Goldstein', 'CUSTOMER', true, true);

-- =====================================================
-- 2. Create Sample Businesses (Tel Aviv area)
-- =====================================================

-- Get category IDs
DO $$
DECLARE
    hair_salon_id UUID;
    barber_id UUID;
    spa_id UUID;
    dentist_id UUID;
    trainer_id UUID;
BEGIN
    SELECT id INTO hair_salon_id FROM categories WHERE slug = 'hair-salon';
    SELECT id INTO barber_id FROM categories WHERE slug = 'barber-shop';
    SELECT id INTO spa_id FROM categories WHERE slug = 'spa-massage';
    SELECT id INTO dentist_id FROM categories WHERE slug = 'dentist';
    SELECT id INTO trainer_id FROM categories WHERE slug = 'personal-trainer';

    -- Business 1: Hair Salon in Tel Aviv
    INSERT INTO businesses (id, owner_id, name, description, category_id, phone, email,
        address_line1, city, country, location, status, is_verified, rating_avg, rating_count)
    VALUES (
        'aaaa1111-1111-1111-1111-111111111111',
        '11111111-1111-1111-1111-111111111111',
        'Salon Bella',
        'Premium hair salon in the heart of Tel Aviv. Specializing in cuts, coloring, and styling for all hair types.',
        hair_salon_id,
        '+972501111111',
        'info@salonbella.co.il',
        '123 Dizengoff Street',
        'Tel Aviv',
        'Israel',
        ST_SetSRID(ST_MakePoint(34.7749, 32.0853), 4326)::geography,
        'ACTIVE',
        true,
        4.8,
        156
    );

    -- Business 2: Barber Shop in Tel Aviv
    INSERT INTO businesses (id, owner_id, name, description, category_id, phone, email,
        address_line1, city, country, location, status, is_verified, rating_avg, rating_count)
    VALUES (
        'aaaa2222-2222-2222-2222-222222222222',
        '22222222-2222-2222-2222-222222222222',
        'The Gentleman''s Cut',
        'Classic barbershop with a modern twist. Expert beard trims and traditional hot towel shaves.',
        barber_id,
        '+972502222222',
        'hello@gentlemanscut.co.il',
        '45 Rothschild Boulevard',
        'Tel Aviv',
        'Israel',
        ST_SetSRID(ST_MakePoint(34.7721, 32.0636), 4326)::geography,
        'ACTIVE',
        true,
        4.9,
        243
    );

    -- Business 3: Spa in Tel Aviv
    INSERT INTO businesses (id, owner_id, name, description, category_id, phone, email,
        address_line1, city, country, location, status, is_verified, rating_avg, rating_count)
    VALUES (
        'aaaa3333-3333-3333-3333-333333333333',
        '33333333-3333-3333-3333-333333333333',
        'Zen Wellness Spa',
        'Escape the city stress with our relaxing treatments. Swedish massage, deep tissue, aromatherapy and more.',
        spa_id,
        '+972503333333',
        'book@zenwellness.co.il',
        '78 Ben Yehuda Street',
        'Tel Aviv',
        'Israel',
        ST_SetSRID(ST_MakePoint(34.7692, 32.0873), 4326)::geography,
        'ACTIVE',
        true,
        4.7,
        189
    );

    -- Business 4: Dental Clinic in Tel Aviv
    INSERT INTO businesses (id, owner_id, name, description, category_id, phone, email,
        address_line1, city, country, location, status, is_verified, rating_avg, rating_count)
    VALUES (
        'aaaa4444-4444-4444-4444-444444444444',
        '44444444-4444-4444-4444-444444444444',
        'Smile Dental Clinic',
        'Modern dental care with the latest technology. General dentistry, cosmetic procedures, and emergency care.',
        dentist_id,
        '+972504444444',
        'appointments@smileclinic.co.il',
        '200 Ibn Gabirol Street',
        'Tel Aviv',
        'Israel',
        ST_SetSRID(ST_MakePoint(34.7818, 32.0804), 4326)::geography,
        'ACTIVE',
        true,
        4.6,
        312
    );

    -- Business 5: Personal Trainer (slightly outside Tel Aviv - Ramat Gan)
    INSERT INTO businesses (id, owner_id, name, description, category_id, phone, email,
        address_line1, city, country, location, status, is_verified, rating_avg, rating_count)
    VALUES (
        'aaaa5555-5555-5555-5555-555555555555',
        '11111111-1111-1111-1111-111111111111',
        'FitPro Training',
        'Personal training sessions tailored to your goals. Weight loss, muscle building, and functional fitness.',
        trainer_id,
        '+972505555555',
        'train@fitpro.co.il',
        '15 Jabotinsky Street',
        'Ramat Gan',
        'Israel',
        ST_SetSRID(ST_MakePoint(34.8113, 32.0684), 4326)::geography,
        'ACTIVE',
        false,
        4.5,
        87
    );
END $$;

-- =====================================================
-- 3. Create Services for Each Business
-- =====================================================

-- Salon Bella Services
INSERT INTO services (id, business_id, name, description, duration_mins, price, currency, is_active, sort_order)
VALUES
    (uuid_generate_v4(), 'aaaa1111-1111-1111-1111-111111111111', 'Women''s Haircut', 'Wash, cut, and blow dry', 60, 180.00, 'ILS', true, 1),
    (uuid_generate_v4(), 'aaaa1111-1111-1111-1111-111111111111', 'Men''s Haircut', 'Classic cut with styling', 30, 80.00, 'ILS', true, 2),
    (uuid_generate_v4(), 'aaaa1111-1111-1111-1111-111111111111', 'Hair Coloring', 'Full color treatment', 120, 350.00, 'ILS', true, 3),
    (uuid_generate_v4(), 'aaaa1111-1111-1111-1111-111111111111', 'Highlights', 'Partial or full highlights', 90, 280.00, 'ILS', true, 4),
    (uuid_generate_v4(), 'aaaa1111-1111-1111-1111-111111111111', 'Blow Dry', 'Professional blow dry styling', 30, 100.00, 'ILS', true, 5);

-- The Gentleman's Cut Services
INSERT INTO services (id, business_id, name, description, duration_mins, price, currency, is_active, sort_order)
VALUES
    (uuid_generate_v4(), 'aaaa2222-2222-2222-2222-222222222222', 'Classic Haircut', 'Traditional cut with hot towel finish', 30, 70.00, 'ILS', true, 1),
    (uuid_generate_v4(), 'aaaa2222-2222-2222-2222-222222222222', 'Beard Trim', 'Shape and trim beard', 20, 40.00, 'ILS', true, 2),
    (uuid_generate_v4(), 'aaaa2222-2222-2222-2222-222222222222', 'Hot Towel Shave', 'Luxurious traditional shave', 30, 60.00, 'ILS', true, 3),
    (uuid_generate_v4(), 'aaaa2222-2222-2222-2222-222222222222', 'Haircut + Beard', 'Complete grooming package', 45, 100.00, 'ILS', true, 4),
    (uuid_generate_v4(), 'aaaa2222-2222-2222-2222-222222222222', 'Kids Haircut', 'For children under 12', 20, 50.00, 'ILS', true, 5);

-- Zen Wellness Spa Services
INSERT INTO services (id, business_id, name, description, duration_mins, price, currency, is_active, sort_order)
VALUES
    (uuid_generate_v4(), 'aaaa3333-3333-3333-3333-333333333333', 'Swedish Massage', 'Relaxing full body massage', 60, 280.00, 'ILS', true, 1),
    (uuid_generate_v4(), 'aaaa3333-3333-3333-3333-333333333333', 'Deep Tissue Massage', 'Intensive muscle therapy', 60, 320.00, 'ILS', true, 2),
    (uuid_generate_v4(), 'aaaa3333-3333-3333-3333-333333333333', 'Hot Stone Therapy', 'Heated stone relaxation', 75, 380.00, 'ILS', true, 3),
    (uuid_generate_v4(), 'aaaa3333-3333-3333-3333-333333333333', 'Aromatherapy', 'Essential oil massage', 60, 300.00, 'ILS', true, 4),
    (uuid_generate_v4(), 'aaaa3333-3333-3333-3333-333333333333', 'Couples Massage', 'Side by side relaxation', 60, 520.00, 'ILS', true, 5);

-- Smile Dental Clinic Services
INSERT INTO services (id, business_id, name, description, duration_mins, price, currency, is_active, sort_order)
VALUES
    (uuid_generate_v4(), 'aaaa4444-4444-4444-4444-444444444444', 'Dental Checkup', 'Comprehensive examination', 30, 150.00, 'ILS', true, 1),
    (uuid_generate_v4(), 'aaaa4444-4444-4444-4444-444444444444', 'Teeth Cleaning', 'Professional cleaning and polish', 45, 250.00, 'ILS', true, 2),
    (uuid_generate_v4(), 'aaaa4444-4444-4444-4444-444444444444', 'Teeth Whitening', 'Professional whitening treatment', 60, 800.00, 'ILS', true, 3),
    (uuid_generate_v4(), 'aaaa4444-4444-4444-4444-444444444444', 'Filling', 'Composite tooth filling', 45, 400.00, 'ILS', true, 4),
    (uuid_generate_v4(), 'aaaa4444-4444-4444-4444-444444444444', 'Emergency Consultation', 'Urgent dental care', 30, 200.00, 'ILS', true, 5);

-- FitPro Training Services
INSERT INTO services (id, business_id, name, description, duration_mins, price, currency, is_active, sort_order)
VALUES
    (uuid_generate_v4(), 'aaaa5555-5555-5555-5555-555555555555', 'Personal Training Session', 'One-on-one workout', 60, 200.00, 'ILS', true, 1),
    (uuid_generate_v4(), 'aaaa5555-5555-5555-5555-555555555555', 'Fitness Assessment', 'Body composition and fitness test', 45, 150.00, 'ILS', true, 2),
    (uuid_generate_v4(), 'aaaa5555-5555-5555-5555-555555555555', 'Nutrition Consultation', 'Diet and meal planning', 60, 180.00, 'ILS', true, 3),
    (uuid_generate_v4(), 'aaaa5555-5555-5555-5555-555555555555', 'Partner Training', 'Train with a friend', 60, 300.00, 'ILS', true, 4);

-- =====================================================
-- 4. Create Working Hours for Each Business
-- =====================================================

-- Salon Bella (Sun-Thu 9-19, Fri 9-14, Sat closed)
INSERT INTO working_hours (business_id, day_of_week, start_time, end_time, is_closed) VALUES
    ('aaaa1111-1111-1111-1111-111111111111', 0, '09:00', '19:00', false),
    ('aaaa1111-1111-1111-1111-111111111111', 1, '09:00', '19:00', false),
    ('aaaa1111-1111-1111-1111-111111111111', 2, '09:00', '19:00', false),
    ('aaaa1111-1111-1111-1111-111111111111', 3, '09:00', '19:00', false),
    ('aaaa1111-1111-1111-1111-111111111111', 4, '09:00', '19:00', false),
    ('aaaa1111-1111-1111-1111-111111111111', 5, '09:00', '14:00', false),
    ('aaaa1111-1111-1111-1111-111111111111', 6, NULL, NULL, true);

-- The Gentleman's Cut (Sun-Thu 8-20, Fri 8-15, Sat closed)
INSERT INTO working_hours (business_id, day_of_week, start_time, end_time, is_closed) VALUES
    ('aaaa2222-2222-2222-2222-222222222222', 0, '08:00', '20:00', false),
    ('aaaa2222-2222-2222-2222-222222222222', 1, '08:00', '20:00', false),
    ('aaaa2222-2222-2222-2222-222222222222', 2, '08:00', '20:00', false),
    ('aaaa2222-2222-2222-2222-222222222222', 3, '08:00', '20:00', false),
    ('aaaa2222-2222-2222-2222-222222222222', 4, '08:00', '20:00', false),
    ('aaaa2222-2222-2222-2222-222222222222', 5, '08:00', '15:00', false),
    ('aaaa2222-2222-2222-2222-222222222222', 6, NULL, NULL, true);

-- Zen Wellness Spa (Sun-Thu 10-21, Fri 10-16, Sat closed) with lunch break
INSERT INTO working_hours (business_id, day_of_week, start_time, end_time, is_closed, break_start, break_end) VALUES
    ('aaaa3333-3333-3333-3333-333333333333', 0, '10:00', '21:00', false, '14:00', '15:00'),
    ('aaaa3333-3333-3333-3333-333333333333', 1, '10:00', '21:00', false, '14:00', '15:00'),
    ('aaaa3333-3333-3333-3333-333333333333', 2, '10:00', '21:00', false, '14:00', '15:00'),
    ('aaaa3333-3333-3333-3333-333333333333', 3, '10:00', '21:00', false, '14:00', '15:00'),
    ('aaaa3333-3333-3333-3333-333333333333', 4, '10:00', '21:00', false, '14:00', '15:00'),
    ('aaaa3333-3333-3333-3333-333333333333', 5, '10:00', '16:00', false, NULL, NULL),
    ('aaaa3333-3333-3333-3333-333333333333', 6, NULL, NULL, true, NULL, NULL);

-- Smile Dental Clinic (Sun-Thu 8-17, Fri-Sat closed) with lunch break
INSERT INTO working_hours (business_id, day_of_week, start_time, end_time, is_closed, break_start, break_end) VALUES
    ('aaaa4444-4444-4444-4444-444444444444', 0, '08:00', '17:00', false, '13:00', '14:00'),
    ('aaaa4444-4444-4444-4444-444444444444', 1, '08:00', '17:00', false, '13:00', '14:00'),
    ('aaaa4444-4444-4444-4444-444444444444', 2, '08:00', '17:00', false, '13:00', '14:00'),
    ('aaaa4444-4444-4444-4444-444444444444', 3, '08:00', '17:00', false, '13:00', '14:00'),
    ('aaaa4444-4444-4444-4444-444444444444', 4, '08:00', '17:00', false, '13:00', '14:00'),
    ('aaaa4444-4444-4444-4444-444444444444', 5, NULL, NULL, true, NULL, NULL),
    ('aaaa4444-4444-4444-4444-444444444444', 6, NULL, NULL, true, NULL, NULL);

-- FitPro Training (Sun-Thu 6-21, Fri 6-14, Sat closed)
INSERT INTO working_hours (business_id, day_of_week, start_time, end_time, is_closed) VALUES
    ('aaaa5555-5555-5555-5555-555555555555', 0, '06:00', '21:00', false),
    ('aaaa5555-5555-5555-5555-555555555555', 1, '06:00', '21:00', false),
    ('aaaa5555-5555-5555-5555-555555555555', 2, '06:00', '21:00', false),
    ('aaaa5555-5555-5555-5555-555555555555', 3, '06:00', '21:00', false),
    ('aaaa5555-5555-5555-5555-555555555555', 4, '06:00', '21:00', false),
    ('aaaa5555-5555-5555-5555-555555555555', 5, '06:00', '14:00', false),
    ('aaaa5555-5555-5555-5555-555555555555', 6, NULL, NULL, true);
