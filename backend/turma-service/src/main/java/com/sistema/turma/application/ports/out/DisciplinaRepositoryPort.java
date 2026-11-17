package com.sistema.turma.application.ports.out;

import com.sistema.turma.domain.model.Disciplina;
import java.util.List;
import java.util.Optional;

public interface DisciplinaRepositoryPort {
    List<Disciplina> buscarTodos();
    Optional<Disciplina> buscarPorId(Long id);
    Disciplina salvar(Disciplina disciplina);
    void excluir(Long id);
    boolean existePorId(Long id);
    /** Código já usado por OUTRA disciplina (idIgnorado = a própria disciplina na edição; null no cadastro). */
    boolean existePorCodigo(String codigo, Long idIgnorado);
}
