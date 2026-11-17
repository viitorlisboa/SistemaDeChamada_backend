package com.sistema.turma.application.ports.out;

import com.sistema.turma.domain.model.Turma;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TurmaRepositoryPort {
    List<Turma> buscarTodos();
    List<Turma> buscarPorIds(Collection<Long> ids);
    List<Turma> buscarPorProfessorId(Long professorId);
    Optional<Turma> buscarPorId(Long id);
    Turma salvar(Turma turma);
    void excluir(Long id);
    boolean existePorId(Long id);
    boolean existePorProfessorId(Long professorId);
    boolean existePorDisciplinaId(Long disciplinaId);
    /** Turma com o mesmo nome na mesma disciplina, ignorando a própria (idIgnorado null no cadastro). */
    boolean existePorNomeEDisciplina(String nome, Long disciplinaId, Long idIgnorado);
    void incrementarAulasMinistradas(Long turmaId);
}
