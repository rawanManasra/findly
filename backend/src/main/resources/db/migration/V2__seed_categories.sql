-- =====================================================
-- Seed Categories
-- =====================================================

INSERT INTO categories (id, name, slug, icon, sort_order) VALUES
    (uuid_generate_v4(), 'Beauty & Wellness', 'beauty-wellness', 'spa', 1),
    (uuid_generate_v4(), 'Health & Medical', 'health-medical', 'medical', 2),
    (uuid_generate_v4(), 'Home Services', 'home-services', 'home', 3),
    (uuid_generate_v4(), 'Automotive', 'automotive', 'car', 4),
    (uuid_generate_v4(), 'Professional Services', 'professional-services', 'briefcase', 5),
    (uuid_generate_v4(), 'Fitness & Sports', 'fitness-sports', 'fitness', 6),
    (uuid_generate_v4(), 'Education & Tutoring', 'education-tutoring', 'school', 7),
    (uuid_generate_v4(), 'Pet Services', 'pet-services', 'pets', 8);

-- Sub-categories for Beauty & Wellness
INSERT INTO categories (name, slug, icon, parent_id, sort_order)
SELECT 'Hair Salon', 'hair-salon', 'cut', id, 1 FROM categories WHERE slug = 'beauty-wellness';

INSERT INTO categories (name, slug, icon, parent_id, sort_order)
SELECT 'Barber Shop', 'barber-shop', 'content-cut', id, 2 FROM categories WHERE slug = 'beauty-wellness';

INSERT INTO categories (name, slug, icon, parent_id, sort_order)
SELECT 'Nail Salon', 'nail-salon', 'brush', id, 3 FROM categories WHERE slug = 'beauty-wellness';

INSERT INTO categories (name, slug, icon, parent_id, sort_order)
SELECT 'Spa & Massage', 'spa-massage', 'spa', id, 4 FROM categories WHERE slug = 'beauty-wellness';

-- Sub-categories for Health & Medical
INSERT INTO categories (name, slug, icon, parent_id, sort_order)
SELECT 'Dentist', 'dentist', 'tooth', id, 1 FROM categories WHERE slug = 'health-medical';

INSERT INTO categories (name, slug, icon, parent_id, sort_order)
SELECT 'Doctor', 'doctor', 'stethoscope', id, 2 FROM categories WHERE slug = 'health-medical';

INSERT INTO categories (name, slug, icon, parent_id, sort_order)
SELECT 'Physiotherapy', 'physiotherapy', 'accessibility', id, 3 FROM categories WHERE slug = 'health-medical';

-- Sub-categories for Home Services
INSERT INTO categories (name, slug, icon, parent_id, sort_order)
SELECT 'Cleaning', 'cleaning', 'cleaning', id, 1 FROM categories WHERE slug = 'home-services';

INSERT INTO categories (name, slug, icon, parent_id, sort_order)
SELECT 'Plumbing', 'plumbing', 'plumbing', id, 2 FROM categories WHERE slug = 'home-services';

INSERT INTO categories (name, slug, icon, parent_id, sort_order)
SELECT 'Electrician', 'electrician', 'bolt', id, 3 FROM categories WHERE slug = 'home-services';

-- Sub-categories for Fitness & Sports
INSERT INTO categories (name, slug, icon, parent_id, sort_order)
SELECT 'Personal Trainer', 'personal-trainer', 'fitness-center', id, 1 FROM categories WHERE slug = 'fitness-sports';

INSERT INTO categories (name, slug, icon, parent_id, sort_order)
SELECT 'Yoga Studio', 'yoga-studio', 'self-improvement', id, 2 FROM categories WHERE slug = 'fitness-sports';
