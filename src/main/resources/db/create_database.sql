-- Verifica se o banco de dados existe
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'techelp')
BEGIN
    CREATE DATABASE techelp;
END
GO 