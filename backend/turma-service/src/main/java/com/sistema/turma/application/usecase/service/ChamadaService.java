package com.sistema.turma.application.usecase.service;

import com.sistema.turma.application.exception.NaoEncontradoException;
import com.sistema.turma.application.exception.RegraNegocioException;
import com.sistema.turma.application.ports.in.GerenciarChamadaUseCase;
import com.sistema.turma.application.ports.out.ChamadaEventPublisherPort;
import com.sistema.turma.application.ports.out.ChamadaRepositoryPort;
import com.sistema.turma.application.ports.out.PresencaRepositoryPort;
import com.sistema.turma.application.ports.out.TurmaRepositoryPort;
import com.sistema.turma.domain.event.ChamadaFinalizadaEvent;
import com.sistema.turma.domain.model.Chamada;
import com.sistema.turma.domain.model.StatusChamada;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ChamadaService implements GerenciarChamadaUseCase {

    private final ChamadaRepositoryPort chamadaRepositoryPort;
    private final PresencaRepositoryPort presencaRepositoryPort;
    private final ChamadaEventPublisherPort chamadaEventPublisherPort; // grava no Outbox (mesma transação)
    private final TurmaRepositoryPort turmaRepositoryPort;
    private final AcessoTurma acessoTurma;

    @Override
    public Chamada criar(Chamada chamada) {
        acessoTurma.verificar(chamada.getTurmaId());
        if (chamadaRepositoryPort.existe(chamada.getTurmaId(), chamada.getData(), chamada.getHorario())) {
            throw new RegraNegocioException("Já existe chamada para a turma " + chamada.getTurmaId()
                    + " em " + chamada.getData() + " às " + chamada.getHorario() + ".");
        }
        chamada.setStatus(StatusChamada.ABERTA);
        return chamadaRepositoryPort.salvar(chamada);
    }

    @Override
    public Chamada buscarPorId(Long id) {
        Chamada chamada = buscarOuFalhar(id);
        acessoTurma.verificar(chamada.getTurmaId());
        return chamada;
    }

    @Override
    public List<Chamada> listarPorTurma(Long turmaId) {
        acessoTurma.verificar(turmaId);
        return chamadaRepositoryPort.listarPorTurma(turmaId);
    }

    /**
     * 404 se a chamada não existir; 403 se não for da turma do professor;
     * 409 se já estiver finalizada ou se a turma já tiver outra chamada na nova data e horário.
     */
    @Override
    @Transactional
    public Chamada atualizar(Long id, Chamada dados) {
        Chamada chamada = buscarPorId(id);
        if (chamada.getStatus() == StatusChamada.FINALIZADA) {
            throw new RegraNegocioException("Não é possível alterar uma chamada finalizada.");
        }
        boolean mudou = !Objects.equals(chamada.getData(), dados.getData())
                || !Objects.equals(chamada.getHorario(), dados.getHorario());
        if (mudou && chamadaRepositoryPort.existe(chamada.getTurmaId(), dados.getData(), dados.getHorario())) {
            throw new RegraNegocioException("Já existe chamada para a turma " + chamada.getTurmaId()
                    + " em " + dados.getData() + " às " + dados.getHorario() + ".");
        }
        chamada.setData(dados.getData());
        chamada.setHorario(dados.getHorario());
        return chamadaRepositoryPort.salvar(chamada);
    }

    /**
     * Status FINALIZADA, aulas ministradas da turma e evento (Outbox) na MESMA transação.
     * O OutboxRelay envia o evento ao Service Bus depois (consumido pelo aluno-service);
     * se o Service Bus cair, o evento não se perde.
     * Idempotente: finalizar de novo não gera evento duplicado.
     */
    @Override
    @Transactional
    public void finalizar(Long id) {
        Chamada chamada = buscarPorId(id);
        if (chamada.getStatus() == StatusChamada.FINALIZADA) {
            return;
        }
        chamada.setStatus(StatusChamada.FINALIZADA);
        Chamada chamadaSalva = chamadaRepositoryPort.salvar(chamada);

        // Mesma transação: a turma conta a aula ministrada sem depender do próprio evento
        turmaRepositoryPort.incrementarAulasMinistradas(chamadaSalva.getTurmaId());

        chamadaEventPublisherPort.publicarChamadaFinalizada(new ChamadaFinalizadaEvent(
                chamadaSalva.getId(),
                chamadaSalva.getTurmaId(),
                chamadaSalva.getData(),
                chamadaSalva.getStatus().name()));
    }

    @Override
    @Transactional
    public void excluir(Long id) {
        Chamada chamada = buscarPorId(id);
        if (chamada.getStatus() == StatusChamada.FINALIZADA) {
            throw new RegraNegocioException("Não é possível excluir uma chamada finalizada.");
        }
        presencaRepositoryPort.excluirPorChamada(id);
        chamadaRepositoryPort.excluir(id);
    }

    Chamada buscarOuFalhar(Long id) {
        return chamadaRepositoryPort.buscarPorId(id)
                .orElseThrow(() -> new NaoEncontradoException("Chamada " + id + " não encontrada."));
    }
}
