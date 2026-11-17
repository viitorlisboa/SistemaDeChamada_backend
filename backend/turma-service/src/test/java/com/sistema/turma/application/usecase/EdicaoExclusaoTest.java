package com.sistema.turma.application.usecase;

import com.sistema.turma.application.exception.RegraNegocioException;
import com.sistema.turma.application.ports.out.*;
import com.sistema.turma.application.security.Perfil;
import com.sistema.turma.application.security.UsuarioLogado;
import com.sistema.turma.application.usecase.service.AcessoTurma;
import com.sistema.turma.application.usecase.service.ChamadaService;
import com.sistema.turma.application.usecase.service.DisciplinaService;
import com.sistema.turma.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Regras de edição (PUT) de chamada e disciplina: 409 para chamada finalizada e para duplicidades. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EdicaoExclusaoTest {

    @Mock private TurmaRepositoryPort turmaRepositoryPort;
    @Mock private DisciplinaRepositoryPort disciplinaRepositoryPort;
    @Mock private ChamadaRepositoryPort chamadaRepositoryPort;
    @Mock private PresencaRepositoryPort presencaRepositoryPort;
    @Mock private ChamadaEventPublisherPort chamadaEventPublisherPort;
    @Mock private UsuarioLogadoPort usuarioLogadoPort;

    private static final UsuarioLogado ADMIN = new UsuarioLogado(1L, "admin", Perfil.ADMIN, null);

    private ChamadaService chamadaService;
    private Chamada chamada;

    @BeforeEach
    void setUp() {
        when(usuarioLogadoPort.atual()).thenReturn(ADMIN);
        chamadaService = new ChamadaService(chamadaRepositoryPort, presencaRepositoryPort, chamadaEventPublisherPort,
                turmaRepositoryPort, new AcessoTurma(turmaRepositoryPort, usuarioLogadoPort));

        Turma turma = new Turma();
        turma.setId(1L);
        Professor professor = new Professor();
        professor.setId(5L);
        turma.setProfessor(professor);
        when(turmaRepositoryPort.buscarPorId(1L)).thenReturn(Optional.of(turma));

        chamada = new Chamada();
        chamada.setId(100L);
        chamada.setTurmaId(1L);
        chamada.setData(LocalDate.of(2026, 11, 17));
        chamada.setHorario(LocalTime.of(8, 0));
        chamada.setStatus(StatusChamada.ABERTA);
        when(chamadaRepositoryPort.buscarPorId(100L)).thenReturn(Optional.of(chamada));
        when(chamadaRepositoryPort.salvar(any(Chamada.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private static Chamada novosDados(LocalDate data, LocalTime horario) {
        Chamada dados = new Chamada();
        dados.setData(data);
        dados.setHorario(horario);
        return dados;
    }

    /** 1. Edita data e horário (LocalTime) de chamada aberta. */
    @Test
    void editaDataEHorarioDeChamadaAberta() {
        Chamada editada = chamadaService.atualizar(100L, novosDados(LocalDate.of(2026, 11, 18), LocalTime.of(10, 30)));

        assertThat(editada.getData()).isEqualTo(LocalDate.of(2026, 11, 18));
        assertThat(editada.getHorario()).isEqualTo(LocalTime.of(10, 30));
        assertThat(editada.getTurmaId()).isEqualTo(1L);
    }

    /** 2. Chamada finalizada não pode ser editada; e o novo horário não pode repetir outra chamada da turma. */
    @Test
    void naoEditaChamadaFinalizadaNemDuplicaHorario() {
        when(chamadaRepositoryPort.existe(1L, LocalDate.of(2026, 11, 18), LocalTime.of(9, 0))).thenReturn(true);
        assertThatThrownBy(() -> chamadaService.atualizar(100L, novosDados(LocalDate.of(2026, 11, 18), LocalTime.of(9, 0))))
                .isInstanceOf(RegraNegocioException.class);

        chamada.setStatus(StatusChamada.FINALIZADA);
        assertThatThrownBy(() -> chamadaService.atualizar(100L, novosDados(LocalDate.of(2026, 11, 19), LocalTime.of(9, 0))))
                .isInstanceOf(RegraNegocioException.class);
        verify(chamadaRepositoryPort, never()).salvar(any());
    }

    /** 3. Disciplina não pode ser editada para o código de outra disciplina. */
    @Test
    void naoEditaDisciplinaParaCodigoDeOutra() {
        Disciplina existente = new Disciplina();
        existente.setId(2L);
        existente.setNome("Cloud");
        existente.setCodigo("CLD1");
        when(disciplinaRepositoryPort.buscarPorId(2L)).thenReturn(Optional.of(existente));
        when(disciplinaRepositoryPort.existePorCodigo("MAT1", 2L)).thenReturn(true);

        Disciplina dados = new Disciplina();
        dados.setNome("Cloud");
        dados.setCodigo(" mat1 ");
        DisciplinaService service = new DisciplinaService(disciplinaRepositoryPort, turmaRepositoryPort, usuarioLogadoPort);

        assertThatThrownBy(() -> service.atualizar(2L, dados)).isInstanceOf(RegraNegocioException.class);
        verify(disciplinaRepositoryPort, never()).salvar(any());
    }
}
