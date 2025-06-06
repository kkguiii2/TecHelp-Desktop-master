-- Verifica se o banco de dados existe
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'techelp')
BEGIN
    CREATE DATABASE techelp;
END
GO

USE techelp;
GO

-- Tabela de usuários
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[usuarios]') AND type in (N'U'))
BEGIN
    CREATE TABLE [dbo].[usuarios] (
        [id] BIGINT IDENTITY(1,1) PRIMARY KEY,
        [nome] VARCHAR(100) NOT NULL,
        [email] VARCHAR(100) NOT NULL UNIQUE,
        [senha] VARCHAR(255) NOT NULL,
        [tipo] VARCHAR(20) NOT NULL,
        [telefone] VARCHAR(20),
        [data_criacao] DATETIME2 NOT NULL,
        [ultimo_acesso] DATETIME2,
        [lgpd_aceite] BIT DEFAULT 0,
        [data_aceite_lgpd] DATETIME2
    );
END
GO

-- Tabela de permissões de usuário
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[permissoes_usuario]') AND type in (N'U'))
BEGIN
    CREATE TABLE permissoes_usuario (
        usuario_id BIGINT NOT NULL,
        permissao VARCHAR(50) NOT NULL,
        CONSTRAINT PK_permissoes_usuario PRIMARY KEY (usuario_id, permissao),
        CONSTRAINT FK_permissoes_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
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
        FOREIGN KEY ([solicitante_id]) REFERENCES [dbo].[usuarios]([id]),
        FOREIGN KEY ([tecnico_id]) REFERENCES [dbo].[usuarios]([id])
    );
END
GO

-- Tabela de interações
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[interacoes]') AND type in (N'U'))
BEGIN
    CREATE TABLE interacoes (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        mensagem VARCHAR(2000) NOT NULL,
        usuario_id BIGINT NOT NULL,
        chamado_id BIGINT NOT NULL,
        data_hora DATETIME2 NOT NULL,
        tipo VARCHAR(20) NOT NULL,
        CONSTRAINT FK_interacoes_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
        CONSTRAINT FK_interacoes_chamado FOREIGN KEY (chamado_id) REFERENCES chamados(id)
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
        FOREIGN KEY ([usuario_id]) REFERENCES [dbo].[usuarios]([id]),
        FOREIGN KEY ([chamado_id]) REFERENCES [dbo].[chamados]([id])
    );
END
GO

-- Índices
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_usuarios_email' AND object_id = OBJECT_ID('usuarios'))
BEGIN
    CREATE INDEX IX_usuarios_email ON usuarios(email);
END
GO

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_chamados_solicitante' AND object_id = OBJECT_ID('chamados'))
BEGIN
    CREATE INDEX IX_chamados_solicitante ON chamados(solicitante_id);
END
GO

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_chamados_tecnico' AND object_id = OBJECT_ID('chamados'))
BEGIN
    CREATE INDEX IX_chamados_tecnico ON chamados(tecnico_id);
END
GO

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_interacoes_chamado' AND object_id = OBJECT_ID('interacoes'))
BEGIN
    CREATE INDEX IX_interacoes_chamado ON interacoes(chamado_id);
END
GO

-- Insere usuário admin padrão se não existir
IF NOT EXISTS (SELECT * FROM [dbo].[usuarios] WHERE [email] = 'admin@techelp.com')
BEGIN
    INSERT INTO [dbo].[usuarios] ([nome], [email], [senha], [tipo], [data_criacao], [lgpd_aceite])
    VALUES ('Administrador', 'admin@techelp.com', 
        '$2a$10$8HxzX3UtpvZU4jyEUgTSyOxZxovXxK4yCWuXJQJGJGjlXCRgKOmrW', -- senha: admin123
        'ADMIN', GETDATE(), 1);
END
GO 