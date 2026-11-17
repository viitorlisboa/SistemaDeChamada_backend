package com.sistema.turma.infrastructure.adapters.out.persistence.entity;

import com.sistema.turma.domain.model.StatusChamada;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Entity(name = "chamadas")
public class ChamadaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long turmaId;

    @Column(nullable = false)
    private LocalDate data;

    // Coluna TIME no SQL Server. Com sendTimeAsDatetime=false (application.properties) o driver
    // envia o LocalTime como TIME, e não como DATETIME, nas consultas e gravações.
    @Column(name = "horario", nullable = false, columnDefinition = "TIME")
    private LocalTime horario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusChamada status;
}
