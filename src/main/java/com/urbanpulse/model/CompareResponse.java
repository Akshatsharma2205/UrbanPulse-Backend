package com.urbanpulse.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompareResponse {
    private SimulationResponse policyA;
    private SimulationResponse policyB;
    private String recommendationSummary;
}
