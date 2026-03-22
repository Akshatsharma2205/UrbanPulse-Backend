package com.urbanpulse.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompareRequest {
    // Backward compatibility parameter
    private String neighborhood;
    
    // The policies to simulate side-by-side
    private String policyA;
    private String policyB;
    
    // Advanced Structural Parameters
    private String city;
    private Integer population;
    private String trafficLevel;
    private String pollutionLevel;
    
    // Time Progression Slider
    private Integer timeHorizon; // In Years
    
    // Strategic Constraints
    private String budget;
    private String priority;
    private String riskLevel;
}
