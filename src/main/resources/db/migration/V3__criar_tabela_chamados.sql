CREATE TABLE chamados (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    descricao TEXT NOT NULL,
    status VARCHAR(50) NOT NULL,
    prioridade VARCHAR(50) NOT NULL,
    solicitante_id BIGINT NOT NULL,
    tecnico_id BIGINT,
    data_abertura TIMESTAMP NOT NULL,
    data_fechamento TIMESTAMP,
    avaliacao INT,
    FOREIGN KEY (solicitante_id) REFERENCES usuarios(id),
    FOREIGN KEY (tecnico_id) REFERENCES usuarios(id)
); 