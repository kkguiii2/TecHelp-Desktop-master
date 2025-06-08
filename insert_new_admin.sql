USE techelp;

INSERT INTO [dbo].[usuarios] (
    [name_user], 
    [email], 
    [password], 
    [type_user], 
    [dept],
    [data_criacao]
)
VALUES (
    'Administrador 1', 
    'admin1@techelp.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewKyBAQ/gWKJRgCy', -- senha: admin123
    'ADMIN', 
    'TI',
    GETDATE()
); 