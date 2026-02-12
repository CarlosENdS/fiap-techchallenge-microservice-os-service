package com.techchallenge.fiap.cargarage.os_service.configuration;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

/**
 * OpenAPI/Swagger configuration.
 */
@Configuration
public class OpenApiConfiguration {

    @Value("${server.port:8081}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Service Order (OS) Microservice API")
                        .version("1.0.0")
                        .description("""
                                API para gerenciamento de Ordens de Serviço (OS) do Car Garage.

                                ## Funcionalidades
                                - Criação e atualização de ordens de serviço
                                - Gerenciamento do ciclo de vida da OS (status workflow)
                                - Aprovação/rejeição de orçamentos por clientes
                                - Cálculo de tempo de execução
                                - Integração com outros microsserviços via eventos (Saga Pattern)

                                ## Workflow de Status
                                RECEIVED → IN_DIAGNOSIS → WAITING_APPROVAL → IN_EXECUTION → FINISHED → DELIVERED

                                A qualquer momento antes de FINISHED, a OS pode ser CANCELLED.
                                """)
                        .contact(new Contact()
                                .name("FIAP Tech Challenge Team")
                                .email("techchallenge@fiap.com.br"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local Development Server")));
    }
}
