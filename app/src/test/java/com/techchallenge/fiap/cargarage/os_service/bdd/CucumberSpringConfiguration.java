package com.techchallenge.fiap.cargarage.os_service.bdd;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Configuration class for Cucumber Spring integration.
 * This class tells Cucumber how to load the Spring context.
 */
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
public class CucumberSpringConfiguration {
}
