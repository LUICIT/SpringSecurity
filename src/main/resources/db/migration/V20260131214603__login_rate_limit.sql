ALTER TABLE public.users
    ADD COLUMN failed_attempts int NOT NULL DEFAULT 0,
    ADD COLUMN locked_until timestamp NULL,
    ADD COLUMN last_failed_at timestamp NULL;