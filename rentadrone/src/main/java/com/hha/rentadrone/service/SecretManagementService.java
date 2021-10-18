package com.hha.rentadrone.service;

import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.security.keyvault.secrets.models.SecretProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Slf4j
@Service
public class SecretManagementService {

    public final SecretClient secretClient;

    SecretManagementService(SecretClient secretClient) {
        this.secretClient = secretClient;
    }

    /**
     * Create a secret tracking number pin valid for 1 week. If the secret
     * already exists in the key vault, then a new version of the secret is created.
     *
     * @param trackingNumber trackingNumber
     */
    public void setSecretTrackingPin(Long trackingNumber, String trackingPin) {
        secretClient.setSecret(new KeyVaultSecret(String.valueOf(trackingNumber), trackingPin)
                .setProperties(new SecretProperties()
                        .setExpiresOn(OffsetDateTime.now().plusWeeks(1))));
        log.info("Secret saved with key {} \n", trackingNumber);
    }

}
