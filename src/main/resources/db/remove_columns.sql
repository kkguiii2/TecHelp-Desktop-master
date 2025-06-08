USE techelp;
GO

-- Remove as colunas da tabela usuarios
IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[usuarios]') AND name = 'telefone')
BEGIN
    ALTER TABLE [dbo].[usuarios] DROP COLUMN [telefone];
END
GO

IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[usuarios]') AND name = 'ultimo_acesso')
BEGIN
    ALTER TABLE [dbo].[usuarios] DROP COLUMN [ultimo_acesso];
END
GO

IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[usuarios]') AND name = 'data_aceite_lgpd')
BEGIN
    ALTER TABLE [dbo].[usuarios] DROP COLUMN [data_aceite_lgpd];
END
GO 