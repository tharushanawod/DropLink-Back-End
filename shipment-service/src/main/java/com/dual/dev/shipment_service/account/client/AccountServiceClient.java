package com.dual.dev.shipment_service.account.client;

import com.dual.dev.shipment_service.account.dto.AccountStatusResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class AccountServiceClient {

    private final RestClient accountServiceRestClient;

    public AccountStatusResponse getCurrentAccountStatus(
            String accessToken
    ) {

        return accountServiceRestClient.get()
                .uri("/api/v1/internal/me/account-status")
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + accessToken
                )
                .retrieve()
                .body(AccountStatusResponse.class);
    }
}