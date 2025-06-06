CREATE TABLE interacoes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    mensagem TEXT NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    usuario_id BIGINT NOT NULL,
    chamado_id BIGINT NOT NULL,
    data_hora TIMESTAMP NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    FOREIGN KEY (chamado_id) REFERENCES chamados(id)
); 