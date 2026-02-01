INSERT INTO public.users
(names,
 last_name,
 second_last_name,
 email,
 password,
 phone,
 user_type,
 created_at,
 updated_at,
 deleted_at)
VALUES ('LUIS RODRIGO',
        'AGUILAR',
        'URIBE',
        'correo_electronico@email.com',
        '$2a$10$yvUJs4/tlIkTbgxa/ErJXO0fL7zim2htxrMkJSV6a/71uFsT8Hrsa', /* Admin123 */
        '1234567890',
        'ADMIN',
        now(),
        now(),
        NULL)
ON CONFLICT (email) DO NOTHING;