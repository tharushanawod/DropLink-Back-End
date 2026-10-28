package com.dual.dev.dispatch_service.dispatch.client;

import com.dual.dev.dispatch_service.dispatch.dto.EligiblePackageResponse;
import com.dual.dev.dispatch_service.dispatch.enums.DispatchType;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
public class ShipmentClient {

    private final RestClient restClient;

    public ShipmentClient(
            RestClient.Builder builder,
            @Value("${services.shipment.url}") String shipmentUrl) {

        this.restClient = builder
                .baseUrl(shipmentUrl)
                .build();
    }

    public List<EligiblePackageResponse> getEligiblePackages(
            UUID branchId,
            DispatchType type) {

        EligiblePackageResponse[] response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/internal/packages/eligible")
                        .queryParam("branchId", branchId)
                        .queryParam("type", type.name())
                        .build())
                .retrieve()
                .body(EligiblePackageResponse[].class);

        return response == null
                ? List.of()
                : Arrays.asList(response);
    }
}