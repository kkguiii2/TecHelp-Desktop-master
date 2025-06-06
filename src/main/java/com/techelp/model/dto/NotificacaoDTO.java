package com.techelp.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificacaoDTO {
    private String mensagem;
    private String tipo;
    private LocalDateTime dataHora;
} 