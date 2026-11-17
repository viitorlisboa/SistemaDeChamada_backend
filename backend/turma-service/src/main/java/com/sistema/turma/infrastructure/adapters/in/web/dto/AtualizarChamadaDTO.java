package com.sistema.turma.infrastructure.adapters.in.web.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

/** Edição de chamada: só data e horário (a turma não muda; para outra turma, crie outra chamada). */
@Data
public class AtualizarChamadaDTO {

    @NotNull(message = "Data é obrigatória")
    private LocalDate data;

    @NotNull(message = "Horário é obrigatório")
    private LocalTime horario;
}
