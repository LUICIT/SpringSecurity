CREATE INDEX IF NOT EXISTS idx_users_locked_until ON public.users (locked_until);
CREATE INDEX IF NOT EXISTS idx_users_deleted_at ON public.users (deleted_at);