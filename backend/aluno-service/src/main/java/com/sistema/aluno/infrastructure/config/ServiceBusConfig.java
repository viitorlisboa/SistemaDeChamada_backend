package com.sistema.aluno.infrastructure.config;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cliente do Azure Service Bus. O consumo de eventos é secundário (atualiza contadores do aluno):
 * se a connection string faltar ou for inválida, o serviço SOBE mesmo assim, registra o problema
 * no log e só deixa de consumir eventos. As mensagens ficam guardadas na subscription até o
 * consumer voltar, então nada se perde.
 * Para desligar de propósito (ex.: rodar localmente): AZURE_SERVICEBUS_ENABLED=false.
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "azure.servicebus.enabled", havingValue = "true", matchIfMissing = true)
public class ServiceBusConfig {

    /** Devolve null (sem bean) quando não há como conectar; o listener trata a ausência. */
    @Bean
    public ServiceBusClientBuilder serviceBusClientBuilder(
            @Value("${azure.servicebus.connection-string:}") String connectionString) {
        if (connectionString == null || connectionString.isBlank()) {
            log.warn("AZURE_SERVICEBUS_CONNECTION_STRING não configurada: o aluno-service sobe, mas NÃO vai consumir "
                    + "eventos de chamada finalizada. Configure a variável (ou AZURE_SERVICEBUS_ENABLED=false).");
            return null;
        }
        try {
            return new ServiceBusClientBuilder().connectionString(connectionString);
        } catch (RuntimeException e) {
            log.error("AZURE_SERVICEBUS_CONNECTION_STRING inválida: o aluno-service sobe sem consumir eventos. Motivo: {}",
                    e.getMessage());
            return null;
        }
    }
}
