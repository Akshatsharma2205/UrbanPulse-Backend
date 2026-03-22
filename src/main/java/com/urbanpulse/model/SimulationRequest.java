package com.urbanpulse.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimulationRequest {
    // Backward compatibility parameter
    private String neighborhood; 
    
    // The policy to simulate
    private String policy;
    
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
