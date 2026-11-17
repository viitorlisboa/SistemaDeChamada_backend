package com.sistema.turma.application.ports.in;

import com.sistema.turma.domain.model.Chamada;
import java.util.List;

public interface GerenciarChamadaUseCase {
    Chamada criar(Chamada chamada);
    Chamada buscarPorId(Long id);
    List<Chamada> listarPorTurma(Long turmaId);
    /** Altera data e horário de uma chamada ainda ABERTA (a turma não muda). */
    Chamada atualizar(Long id, Chamada chamada);
    void finalizar(Long id);
    void excluir(Long id);
}
