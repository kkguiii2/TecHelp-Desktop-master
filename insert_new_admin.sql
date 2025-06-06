USE techelp;

INSERT INTO [dbo].[usuarios] (
    [nome], 
    [email], 
    [senha], 
    [tipo], 
    [data_criacao], 
    [lgpd_aceite]
)
VALUES (
    'Administrador 1', 
    'admin1@techelp.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewKyBAQ/gWKJRgCy', -- senha: admin123
    'ADMIN', 
    GETDATE(), 
    1
); 