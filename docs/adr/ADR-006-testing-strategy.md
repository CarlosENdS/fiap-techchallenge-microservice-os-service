# ADR-006: Estratégia de Testes

## Status

**Aceita** - 2024-02-10

## Contexto

O OS Service precisa de uma estratégia de testes que:

- Garanta qualidade do código e correção das regras de negócio
- Permita refatorações seguras
- Seja executável em CI/CD
- Cubra diferentes níveis (unitário, integração, E2E)
- Teste comportamento de negócio, não apenas implementação

## Decisão

Adotamos uma estratégia de testes em múltiplas camadas, combinando testes unitários, de integração e BDD.

### Pirâmide de Testes

```
        ╱╲
       ╱  ╲
      ╱ E2E╲         (Poucos - Cucumber BDD)
     ╱──────╲
    ╱        ╲
   ╱Integration╲     (Médio - @SpringBootTest)
  ╱────────────╲
 ╱              ╲
╱   Unit Tests   ╲   (Muitos - JUnit + Mockito)
╲────────────────╱
```

### 1. Testes Unitários

**Foco**: Use Cases, Entities, Value Objects

**Framework**: JUnit 5 + Mockito

**Exemplo**:
```java
@ExtendWith(MockitoExtension.class)
class CreateServiceOrderUseCaseTest {
    
    @Mock
    private ServiceOrderGateway gateway;
    
    @InjectMocks
    private CreateServiceOrderUseCase useCase;
    
    @Test
    void shouldCreateOrderWithReceivedStatus() {
        // Given
        var request = new CreateServiceOrderRequestDto(...);
        
        // When
        var result = useCase.execute(request);
        
        // Then
        assertThat(result.status()).isEqualTo("RECEIVED");
        verify(gateway).create(any(ServiceOrder.class));
    }
}
```

### 2. Testes de Integração

**Foco**: Controllers, Repositories, Configuração Spring

**Framework**: @SpringBootTest + H2 Database

**Perfil**: `application-test.properties`

```properties
# application-test.properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop

# Desabilita AWS auto-config para testes
spring.autoconfigure.exclude=\
  io.awspring.cloud.autoconfigure.sqs.SqsAutoConfiguration,\
  io.awspring.cloud.autoconfigure.core.AwsAutoConfiguration,\
  io.awspring.cloud.autoconfigure.core.CredentialsProviderAutoConfiguration,\
  io.awspring.cloud.autoconfigure.core.RegionProviderAutoConfiguration
```

**Exemplo**:
```java
@SpringBootTest
@ActiveProfiles("test")
class ServiceOrderControllerIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void shouldCreateServiceOrder() {
        var response = restTemplate.postForEntity(
            "/api/os-service/service-orders", 
            request, 
            ServiceOrderResponseDto.class
        );
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }
}
```

### 3. Testes BDD (Behavior-Driven Development)

**Foco**: Fluxos de negócio completos

**Framework**: Cucumber 7.18.0 com Gherkin em português

**Vantagens**:
- Documentação executável
- Linguagem ubíqua com stakeholders
- Testa integração real dos componentes

**Estrutura**:
```
src/test/
├── java/
│   └── com/.../bdd/
│       ├── CucumberRunnerTest.java
│       ├── CucumberSpringConfiguration.java
│       └── ServiceOrderSteps.java
└── resources/
    └── features/
        └── service_order.feature
```

**Exemplo Feature**:
```gherkin
# language: pt
Funcionalidade: Gerenciamento de Ordens de Serviço

  Cenário: Criar nova ordem de serviço
    Dado que existe um cliente com ID válido
    E existe um veículo com ID válido
    Quando eu criar uma nova ordem de serviço com reclamação "Motor falhando"
    Então a ordem de serviço deve ser criada com status "RECEIVED"
    E a data de recebimento deve estar preenchida

  Cenário: Fluxo completo de ordem de serviço
    Dado que existe uma ordem de serviço
    Quando eu atualizar o status para "IN_DIAGNOSIS"
    E eu atualizar o status para "WAITING_APPROVAL"
    E o cliente aprovar o orçamento
    E eu atualizar o status para "FINISHED"
    E eu atualizar o status para "DELIVERED"
    Então a ordem de serviço deve ter status "DELIVERED"
    E todas as datas do ciclo de vida devem estar preenchidas
```

### 4. Cobertura de Código

**Ferramenta**: JaCoCo

**Métricas Alvo**:
- Instruction: 80%
- Branch: 70%

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <configuration>
        <rules>
            <rule>
                <limits>
                    <limit>
                        <counter>INSTRUCTION</counter>
                        <minimum>0.80</minimum>
                    </limit>
                    <limit>
                        <counter>BRANCH</counter>
                        <minimum>0.70</minimum>
                    </limit>
                </limits>
            </rule>
        </rules>
    </configuration>
</plugin>
```

## Execução

```bash
# Todos os testes
./mvnw test

# Apenas testes unitários
./mvnw test -Dtest=*Test

# Apenas testes BDD
./mvnw test -Dtest=CucumberRunnerTest

# Com relatório de cobertura
./mvnw test jacoco:report
# Relatório em: target/site/jacoco/index.html

# Pular verificação de cobertura
./mvnw test -Djacoco.skip=true
```

## Consequências

### Positivas

- ✅ **Confiança**: Mudanças podem ser verificadas automaticamente
- ✅ **Documentação**: BDD serve como documentação viva
- ✅ **Qualidade**: Cobertura mínima garante baseline
- ✅ **CI/CD**: Integração completa com pipeline
- ✅ **Isolamento**: Testes de integração usam H2, não AWS real

### Negativas

- ❌ **Tempo**: Testes de integração são mais lentos
- ❌ **Manutenção**: Features BDD precisam ser mantidas
- ❌ **Cobertura**: Métrica pode incentivar testes ruins apenas por cobertura

## Alternativas Consideradas

### 1. Apenas Testes Unitários

**Prós**: Rápidos, isolados

**Contras**: Não testam integração real

**Decisão**: Rejeitado - insuficiente para garantir funcionamento

### 2. Testes E2E com Selenium/Playwright

**Prós**: Testa sistema completo

**Contras**: Lentos, frágeis, difíceis de manter

**Decisão**: Rejeitado - BDD com Cucumber atende nosso caso

### 3. Contract Testing (Pact)

**Prós**: Garante compatibilidade entre serviços

**Contras**: Complexidade adicional

**Decisão**: Considerado para futuro

## Referências

- [Testing Pyramid - Martin Fowler](https://martinfowler.com/bliki/TestPyramid.html)
- [Cucumber Documentation](https://cucumber.io/docs/cucumber/)
- [JaCoCo Documentation](https://www.jacoco.org/jacoco/trunk/doc/)
