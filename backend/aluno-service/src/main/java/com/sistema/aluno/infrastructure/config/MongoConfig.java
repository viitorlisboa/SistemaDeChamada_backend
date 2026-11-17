package com.sistema.aluno.infrastructure.config;

import org.springframework.boot.mongodb.autoconfigure.MongoClientSettingsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Ajuste do cliente MongoDB para o Azure Cosmos DB (API for MongoDB).
 *
 * O Cosmos DB não suporta retryable writes: com retryWrites=true (padrão do driver quando a URI
 * não diz nada) o insert/update falha. Aqui só desligamos o retryWrites; a URI, o TLS e o resto
 * continuam vindo da connection string (SPRING_DATA_MONGODB_URI). O customizer roda depois do
 * padrão do Spring Boot, então vale mesmo que a URI venha sem "retrywrites=false".
 */
@Configuration
public class MongoConfig {

    @Bean
    public MongoClientSettingsBuilderCustomizer cosmosDbSemRetryWrites() {
        return builder -> builder.retryWrites(false);
    }
}
