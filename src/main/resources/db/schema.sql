USE techelp;
GO

-- Tabela de usuários
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[usuarios]') AND type in (N'U'))
BEGIN
    CREATE TABLE [dbo].[usuarios] (
        [id_user] BIGINT IDENTITY(1,1) PRIMARY KEY,
        [name_user] VARCHAR(100) NOT NULL,
        [email] VARCHAR(100) NOT NULL UNIQUE,
        [password] VARCHAR(255) NOT NULL,
        [type_user] VARCHAR(20) NOT NULL,
        [dept] VARCHAR(100) NOT NULL,
        [data_criacao] DATETIME2 NOT NULL
    );
END
GO

-- Tabela de chamados
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[chamados]') AND type in (N'U'))
BEGIN
    CREATE TABLE [dbo].[chamados] (
        [id] BIGINT IDENTITY(1,1) PRIMARY KEY,
        [titulo] VARCHAR(100) NOT NULL,
        [descricao] TEXT NOT NULL,
        [categoria] VARCHAR(50) NOT NULL,
        [prioridade] VARCHAR(20) NOT NULL,
        [status] VARCHAR(20) NOT NULL,
        [solicitante_id] BIGINT NOT NULL,
        [tecnico_id] BIGINT,
        [data_abertura] DATETIME2 NOT NULL,
        [data_fechamento] DATETIME2,
        [solucao] TEXT,
        [avaliacao] INT,
        [comentario_avaliacao] TEXT,
        [tempo_resolucao] BIGINT,
        [categoria_ia] VARCHAR(50),
        FOREIGN KEY ([solicitante_id]) REFERENCES [dbo].[usuarios]([id_user]),
        FOREIGN KEY ([tecnico_id]) REFERENCES [dbo].[usuarios]([id_user])
    );
END
GO

-- Tabela de notificações
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[notificacoes]') AND type in (N'U'))
BEGIN
    CREATE TABLE [dbo].[notificacoes] (
        [id] BIGINT IDENTITY(1,1) PRIMARY KEY,
        [usuario_id] BIGINT NOT NULL,
        [chamado_id] BIGINT NOT NULL,
        [mensagem] TEXT NOT NULL,
        [data_criacao] DATETIME2 NOT NULL,
        [lida] BIT DEFAULT 0,
        FOREIGN KEY ([usuario_id]) REFERENCES [dbo].[usuarios]([id_user]),
        FOREIGN KEY ([chamado_id]) REFERENCES [dbo].[chamados]([id])
    );
END
GO

-- Tabela de permissões de usuário
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[permissoes_usuario]') AND type in (N'U'))
BEGIN
    CREATE TABLE [dbo].[permissoes_usuario] (
        [usuario_id] BIGINT NOT NULL,
        [permissao] VARCHAR(50) NOT NULL,
        PRIMARY KEY ([usuario_id], [permissao]),
        FOREIGN KEY ([usuario_id]) REFERENCES [dbo].[usuarios]([id_user])
    );
END
GO

-- Tabela de interações
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[interacoes]') AND type in (N'U'))
BEGIN
    CREATE TABLE [dbo].[interacoes] (
        [id] BIGINT IDENTITY(1,1) PRIMARY KEY,
        [mensagem] VARCHAR(2000) NOT NULL,
        [usuario_id] BIGINT NOT NULL,
        [chamado_id] BIGINT NOT NULL,
        [data_hora] DATETIME2 NOT NULL,
        [tipo] VARCHAR(20) NOT NULL,
        FOREIGN KEY ([usuario_id]) REFERENCES [dbo].[usuarios]([id_user]),
        FOREIGN KEY ([chamado_id]) REFERENCES [dbo].[chamados]([id])
    );
END
GO

-- Índices
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_usuarios_email' AND object_id = OBJECT_ID('usuarios'))
    CREATE UNIQUE INDEX [IX_usuarios_email] ON [dbo].[usuarios]([email]);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_chamados_solicitante' AND object_id = OBJECT_ID('chamados'))
    CREATE INDEX [IX_chamados_solicitante] ON [dbo].[chamados]([solicitante_id]);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_chamados_tecnico' AND object_id = OBJECT_ID('chamados'))
    CREATE INDEX [IX_chamados_tecnico] ON [dbo].[chamados]([tecnico_id]);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_interacoes_chamado' AND object_id = OBJECT_ID('interacoes'))
    CREATE INDEX [IX_interacoes_chamado] ON [dbo].[interacoes]([chamado_id]);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_notificacoes_usuario' AND object_id = OBJECT_ID('notificacoes'))
    CREATE INDEX [IX_notificacoes_usuario] ON [dbo].[notificacoes]([usuario_id]);
GO

-- Insere usuário admin padrão se não existir
IF NOT EXISTS (SELECT * FROM [dbo].[usuarios] WHERE [email] = 'admin@techelp.com')
BEGIN
    INSERT INTO [dbo].[usuarios] ([name_user], [email], [password], [type_user], [data_criacao])
    VALUES ('Administrador', 'admin@techelp.com',
        '$2a$10$8HxzX3UtpvZU4jyEUgTSyOxZxovXxK4yCWuXJQJGJGjlXCRgKOmrW', -- senha: admin123
        'ADMIN', GETDATE());
END
GO 