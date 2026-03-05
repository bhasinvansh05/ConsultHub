-- consulting_db schema for Neon (PostgreSQL)
-- Aligned with EECS 3311 project specification (UC1-UC12).

BEGIN;

SET search_path TO public;

-- =========================================================
-- Core users and profile data
-- =========================================================
CREATE TABLE IF NOT EXISTS app_users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(320) NOT NULL,
    password_hash VARCHAR(255),
    full_name VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    account_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_app_users_email UNIQUE (email),
    CONSTRAINT ck_app_users_role CHECK (role IN ('CLIENT', 'CONSULTANT', 'ADMIN')),
    CONSTRAINT ck_app_users_status CHECK (account_status IN ('PENDING', 'ACTIVE', 'INACTIVE'))
);

-- =========================================================
-- UC1: Browse Consulting Services
-- =========================================================
CREATE TABLE IF NOT EXISTS consulting_services (
    id BIGSERIAL PRIMARY KEY,
    consultant_id BIGINT NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    service_type VARCHAR(100) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    duration_minutes INTEGER NOT NULL,
    base_price NUMERIC(10, 2) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT ck_consulting_services_duration CHECK (duration_minutes > 0),
    CONSTRAINT ck_consulting_services_base_price CHECK (base_price >= 0)
);

-- =========================================================
-- UC8: Consultant manages availability
-- =========================================================
CREATE TABLE IF NOT EXISTS consultant_availability_slots (
    id BIGSERIAL PRIMARY KEY,
    consultant_id BIGINT NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    start_at TIMESTAMPTZ NOT NULL,
    end_at TIMESTAMPTZ NOT NULL,
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT ck_availability_slot_time_window CHECK (end_at > start_at),
    CONSTRAINT uq_availability_slot UNIQUE (consultant_id, start_at, end_at)
);

-- =========================================================
-- UC2/UC3/UC4/UC9/UC10: Booking lifecycle
-- =========================================================
CREATE TABLE IF NOT EXISTS bookings (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL REFERENCES app_users(id) ON DELETE RESTRICT,
    consultant_id BIGINT NOT NULL REFERENCES app_users(id) ON DELETE RESTRICT,
    service_id BIGINT NOT NULL REFERENCES consulting_services(id) ON DELETE RESTRICT,
    availability_slot_id BIGINT REFERENCES consultant_availability_slots(id) ON DELETE SET NULL,
    requested_start_at TIMESTAMPTZ NOT NULL,
    requested_end_at TIMESTAMPTZ NOT NULL,
    status VARCHAR(30) NOT NULL,
    cancellation_reason VARCHAR(500),
    rejection_reason VARCHAR(500),
    requested_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    consultant_decided_at TIMESTAMPTZ,
    cancelled_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT ck_bookings_time_window CHECK (requested_end_at > requested_start_at),
    CONSTRAINT ck_bookings_status CHECK (
        status IN (
            'REQUESTED',
            'CONFIRMED',
            'PENDING_PAYMENT',
            'PAID',
            'REJECTED',
            'CANCELLED',
            'COMPLETED'
        )
    )
);

CREATE TABLE IF NOT EXISTS booking_status_history (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    from_status VARCHAR(30),
    to_status VARCHAR(30) NOT NULL,
    changed_by_user_id BIGINT REFERENCES app_users(id) ON DELETE SET NULL,
    change_reason VARCHAR(500),
    changed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT ck_booking_status_history_to CHECK (
        to_status IN (
            'REQUESTED',
            'CONFIRMED',
            'PENDING_PAYMENT',
            'PAID',
            'REJECTED',
            'CANCELLED',
            'COMPLETED'
        )
    ),
    CONSTRAINT ck_booking_status_history_from CHECK (
        from_status IS NULL OR from_status IN (
            'REQUESTED',
            'CONFIRMED',
            'PENDING_PAYMENT',
            'PAID',
            'REJECTED',
            'CANCELLED',
            'COMPLETED'
        )
    )
);

-- =========================================================
-- UC6: Manage payment methods
-- =========================================================
CREATE TABLE IF NOT EXISTS payment_methods (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    method_type VARCHAR(30) NOT NULL,
    display_label VARCHAR(120) NOT NULL,
    card_last4 CHAR(4),
    card_expiry_month SMALLINT,
    card_expiry_year SMALLINT,
    paypal_email VARCHAR(320),
    bank_account_last4 CHAR(4),
    bank_routing_last4 CHAR(4),
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT ck_payment_methods_type CHECK (
        method_type IN ('CREDIT_CARD', 'DEBIT_CARD', 'PAYPAL', 'BANK_TRANSFER')
    ),
    CONSTRAINT ck_payment_methods_card_expiry_month CHECK (
        card_expiry_month IS NULL OR (card_expiry_month BETWEEN 1 AND 12)
    ),
    CONSTRAINT ck_payment_methods_card_expiry_year CHECK (
        card_expiry_year IS NULL OR card_expiry_year >= 2000
    )
);

-- =========================================================
-- UC5/UC7: Payment processing and payment history
-- =========================================================
CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL REFERENCES bookings(id) ON DELETE RESTRICT,
    client_id BIGINT NOT NULL REFERENCES app_users(id) ON DELETE RESTRICT,
    payment_method_id BIGINT REFERENCES payment_methods(id) ON DELETE SET NULL,
    transaction_id VARCHAR(64) NOT NULL,
    status VARCHAR(20) NOT NULL,
    amount NUMERIC(10, 2) NOT NULL,
    currency CHAR(3) NOT NULL DEFAULT 'USD',
    failure_reason VARCHAR(500),
    refund_amount NUMERIC(10, 2),
    processed_at TIMESTAMPTZ,
    refunded_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_payments_transaction_id UNIQUE (transaction_id),
    CONSTRAINT ck_payments_status CHECK (
        status IN ('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED')
    ),
    CONSTRAINT ck_payments_amount CHECK (amount >= 0),
    CONSTRAINT ck_payments_refund_amount CHECK (refund_amount IS NULL OR refund_amount >= 0)
);

-- =========================================================
-- UC11: Approve consultant registration
-- =========================================================
CREATE TABLE IF NOT EXISTS consultant_registrations (
    id BIGSERIAL PRIMARY KEY,
    consultant_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    approved_by_admin_id VARCHAR(255),
    decision_reason VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL,
    decided_at TIMESTAMPTZ,

    CONSTRAINT uq_consultant_registrations_consultant_id UNIQUE (consultant_id),
    CONSTRAINT ck_consultant_registrations_status
        CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    CONSTRAINT ck_consultant_registrations_decision_fields
        CHECK (
            (status = 'PENDING' AND approved_by_admin_id IS NULL AND decision_reason IS NULL AND decided_at IS NULL)
            OR
            (status IN ('APPROVED', 'REJECTED') AND approved_by_admin_id IS NOT NULL AND decided_at IS NOT NULL)
        )
);

-- =========================================================
-- UC12: Admin-defined system policies
-- =========================================================
CREATE TABLE IF NOT EXISTS system_policies (
    id BIGSERIAL PRIMARY KEY,
    policy_key VARCHAR(255) NOT NULL,
    policy_value VARCHAR(4000) NOT NULL,
    updated_by_admin_id VARCHAR(255) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT uq_system_policies_policy_key UNIQUE (policy_key),
    CONSTRAINT ck_system_policies_policy_key
        CHECK (
            policy_key IN (
                'CANCELLATION_RULES',
                'PRICING_STRATEGY',
                'NOTIFICATION_SETTINGS',
                'REFUND_POLICY'
            )
        )
);

-- =========================================================
-- Optional notification log (supports system notifications requirement)
-- =========================================================
CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    notification_type VARCHAR(40) NOT NULL,
    payload TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT ck_notifications_type CHECK (
        notification_type IN (
            'BOOKING_REQUESTED',
            'BOOKING_CONFIRMED',
            'BOOKING_REJECTED',
            'BOOKING_CANCELLED',
            'PAYMENT_SUCCESS',
            'PAYMENT_FAILED',
            'PAYMENT_REFUNDED',
            'POLICY_UPDATED'
        )
    )
);

-- =========================================================
-- Indexes
-- =========================================================
CREATE INDEX IF NOT EXISTS idx_consulting_services_consultant_active
    ON consulting_services (consultant_id, is_active);

CREATE INDEX IF NOT EXISTS idx_availability_consultant_start
    ON consultant_availability_slots (consultant_id, start_at);

CREATE INDEX IF NOT EXISTS idx_bookings_client_requested_at
    ON bookings (client_id, requested_at DESC);

CREATE INDEX IF NOT EXISTS idx_bookings_consultant_requested_at
    ON bookings (consultant_id, requested_at DESC);

CREATE INDEX IF NOT EXISTS idx_bookings_status
    ON bookings (status);

CREATE INDEX IF NOT EXISTS idx_booking_status_history_booking_changed_at
    ON booking_status_history (booking_id, changed_at DESC);

CREATE INDEX IF NOT EXISTS idx_payment_methods_client_active
    ON payment_methods (client_id, is_active);

CREATE INDEX IF NOT EXISTS idx_payments_client_created_at
    ON payments (client_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_payments_booking
    ON payments (booking_id);

CREATE INDEX IF NOT EXISTS idx_consultant_registrations_status
    ON consultant_registrations (status);

CREATE INDEX IF NOT EXISTS idx_consultant_registrations_created_at
    ON consultant_registrations (created_at DESC);

CREATE INDEX IF NOT EXISTS idx_system_policies_updated_at
    ON system_policies (updated_at DESC);

CREATE INDEX IF NOT EXISTS idx_notifications_user_created_at
    ON notifications (user_id, created_at DESC);

COMMIT;
