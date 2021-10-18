package com.hha.rentadrone.config;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.hha.rentadrone.RentADroneApplication.ENV_VAR_AZURE_VAULT_URL;

@Slf4j
@Configuration
public class SecretClientConfig {

    @Value("${" + ENV_VAR_AZURE_VAULT_URL + "}")
    private String vaultUrl;

    @SneakyThrows
    @Bean
    public SecretClient buildSecretClient() {
        return new SecretClientBuilder()
                .vaultUrl(vaultUrl)
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildClient();
    }
}
