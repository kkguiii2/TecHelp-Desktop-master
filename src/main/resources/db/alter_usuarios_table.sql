USE techelp;
GO

-- Renomear colunas
EXEC sp_rename 'usuarios.id', 'id_user', 'COLUMN';
EXEC sp_rename 'usuarios.nome', 'name_user', 'COLUMN';
EXEC sp_rename 'usuarios.tipo', 'type_user', 'COLUMN';
EXEC sp_rename 'usuarios.senha', 'password', 'COLUMN';
EXEC sp_rename 'usuarios.departamento', 'dept', 'COLUMN';

-- Remover coluna lgpd_aceite
ALTER TABLE usuarios DROP COLUMN lgpd_aceite;

-- Atualizar as foreign keys que referenciam a coluna id
ALTER TABLE chamados DROP CONSTRAINT FK_chamados_solicitante;
ALTER TABLE chamados DROP CONSTRAINT FK_chamados_tecnico;
ALTER TABLE chamados ADD CONSTRAINT FK_chamados_solicitante FOREIGN KEY (solicitante_id) REFERENCES usuarios(id_user);
ALTER TABLE chamados ADD CONSTRAINT FK_chamados_tecnico FOREIGN KEY (tecnico_id) REFERENCES usuarios(id_user);

ALTER TABLE interacoes DROP CONSTRAINT FK_interacoes_usuario;
ALTER TABLE interacoes ADD CONSTRAINT FK_interacoes_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id_user);

ALTER TABLE notificacoes DROP CONSTRAINT FK_notificacoes_usuario;
ALTER TABLE notificacoes ADD CONSTRAINT FK_notificacoes_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id_user);
GO