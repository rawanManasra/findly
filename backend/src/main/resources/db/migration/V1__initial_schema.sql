-- =====================================================
-- Findly Database Schema - Initial Migration
-- =====================================================

-- Enable extensions (should already be enabled via init-db.sql)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS postgis;

-- =====================================================
-- ENUMS
-- =====================================================

CREATE TYPE user_role AS ENUM ('CUSTOMER', 'BUSINESS_OWNER', 'ADMIN');
CREATE TYPE business_status AS ENUM ('ACTIVE', 'INACTIVE', 'PENDING_APPROVAL', 'SUSPENDED');
CREATE TYPE booking_status AS ENUM ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED', 'COMPLETED', 'NO_SHOW');
CREATE TYPE notification_type AS ENUM ('BOOKING_CREATED', 'BOOKING_APPROVED', 'BOOKING_REJECTED', 'BOOKING_CANCELLED', 'BOOKING_REMINDER');
CREATE TYPE notification_status AS ENUM ('PENDING', 'SENT', 'FAILED');
CREATE TYPE notification_channel AS ENUM ('SMS', 'EMAIL', 'PUSH');

-- =====================================================
-- USERS TABLE
-- =====================================================

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255),
    phone VARCHAR(20) UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100),
    role user_role NOT NULL DEFAULT 'CUSTOMER',
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    phone_verified BOOLEAN NOT NULL DEFAULT FALSE,
    avatar_url VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_users_email ON users(email) WHERE deleted_at IS NULL;
CREATE INDEX idx_users_phone ON users(phone) WHERE deleted_at IS NULL;
CREATE INDEX idx_users_role ON users(role) WHERE deleted_at IS NULL;

-- =====================================================
-- CATEGORIES TABLE
-- =====================================================

CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(100) UNIQUE NOT NULL,
    icon VARCHAR(50),
    parent_id UUID REFERENCES categories(id),
    sort_order INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_categories_parent ON categories(parent_id);
CREATE INDEX idx_categories_slug ON categories(slug) WHERE is_active = TRUE;

-- =====================================================
-- BUSINESSES TABLE (with PostGIS location)
-- =====================================================

CREATE TABLE businesses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    owner_id UUID NOT NULL REFERENCES users(id),
    name VARCHAR(200) NOT NULL,
    description TEXT,
    category_id UUID REFERENCES categories(id),
    phone VARCHAR(20),
    email VARCHAR(255),
    website VARCHAR(500),
    image_url VARCHAR(500),

    -- Address fields
    address_line1 VARCHAR(255),
    address_line2 VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(100) DEFAULT 'Israel',

    -- PostGIS location (GEOGRAPHY for accurate distance calculations)
    location GEOGRAPHY(POINT, 4326),
    location_updated_at TIMESTAMP WITH TIME ZONE,

    -- Status
    status business_status NOT NULL DEFAULT 'PENDING_APPROVAL',
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    rating_avg DECIMAL(3, 2) DEFAULT 0.00,
    rating_count INTEGER DEFAULT 0,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP WITH TIME ZONE
);

-- Spatial index for location queries (CRITICAL for performance)
CREATE INDEX idx_businesses_location ON businesses USING GIST(location);
CREATE INDEX idx_businesses_owner ON businesses(owner_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_businesses_category ON businesses(category_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_businesses_status ON businesses(status) WHERE deleted_at IS NULL;
CREATE INDEX idx_businesses_city ON businesses(city) WHERE deleted_at IS NULL;

-- =====================================================
-- SERVICES TABLE
-- =====================================================

CREATE TABLE services (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    duration_mins INTEGER NOT NULL DEFAULT 30,
    price DECIMAL(10, 2),
    currency VARCHAR(3) DEFAULT 'ILS',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_services_business ON services(business_id) WHERE is_active = TRUE;

-- =====================================================
-- WORKING HOURS TABLE
-- =====================================================

CREATE TABLE working_hours (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    day_of_week SMALLINT NOT NULL CHECK (day_of_week BETWEEN 0 AND 6), -- 0 = Sunday
    start_time TIME,
    end_time TIME,
    is_closed BOOLEAN NOT NULL DEFAULT FALSE,
    break_start TIME,
    break_end TIME,
    UNIQUE(business_id, day_of_week)
);

CREATE INDEX idx_working_hours_business ON working_hours(business_id);

-- =====================================================
-- BOOKINGS TABLE
-- =====================================================

CREATE TABLE bookings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id),
    service_id UUID NOT NULL REFERENCES services(id),
    customer_id UUID REFERENCES users(id), -- nullable for guest bookings

    -- Guest info (used when customer_id is null)
    guest_name VARCHAR(200),
    guest_phone VARCHAR(20),
    guest_email VARCHAR(255),

    -- Appointment details
    date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status booking_status NOT NULL DEFAULT 'PENDING',
    notes TEXT,
    rejection_reason VARCHAR(500),

    -- Tracking timestamps
    booked_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    confirmed_at TIMESTAMP WITH TIME ZONE,
    cancelled_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    -- Ensure either customer_id or guest info is provided
    CONSTRAINT chk_customer_or_guest CHECK (
        customer_id IS NOT NULL OR (guest_name IS NOT NULL AND guest_phone IS NOT NULL)
    )
);

CREATE INDEX idx_bookings_business_date ON bookings(business_id, date);
CREATE INDEX idx_bookings_customer ON bookings(customer_id) WHERE customer_id IS NOT NULL;
CREATE INDEX idx_bookings_status ON bookings(status);
CREATE INDEX idx_bookings_date ON bookings(date);

-- =====================================================
-- NOTIFICATIONS TABLE
-- =====================================================

CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id),
    booking_id UUID REFERENCES bookings(id),
    type notification_type NOT NULL,
    channel notification_channel NOT NULL,
    recipient VARCHAR(255) NOT NULL, -- phone number or email
    message TEXT NOT NULL,
    status notification_status NOT NULL DEFAULT 'PENDING',
    sent_at TIMESTAMP WITH TIME ZONE,
    error_message TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notifications_user ON notifications(user_id);
CREATE INDEX idx_notifications_booking ON notifications(booking_id);
CREATE INDEX idx_notifications_status ON notifications(status);

-- =====================================================
-- REFRESH TOKENS TABLE (for JWT refresh)
-- =====================================================

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL,
    device_info VARCHAR(500),
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_hash ON refresh_tokens(token_hash);

-- =====================================================
-- TRIGGERS for updated_at
-- =====================================================

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_businesses_updated_at
    BEFORE UPDATE ON businesses
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_services_updated_at
    BEFORE UPDATE ON services
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_bookings_updated_at
    BEFORE UPDATE ON bookings
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_categories_updated_at
    BEFORE UPDATE ON categories
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
