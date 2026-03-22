package com.urbanpulse.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urbanpulse.model.CompareRequest;
import com.urbanpulse.model.CompareResponse;
import com.urbanpulse.model.Impact;
import com.urbanpulse.model.SimulationRequest;
import com.urbanpulse.model.SimulationResponse;
import com.urbanpulse.model.Stakeholder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class SimulationService {

    private final AIService aiService;
    private final ObjectMapper objectMapper;

    @Autowired
    public SimulationService(AIService aiService) {
        this.aiService = aiService;
        this.objectMapper = new ObjectMapper();
    }

    public CompareResponse compare(CompareRequest request) {
        SimulationRequest reqA = new SimulationRequest(
            request.getNeighborhood(), request.getPolicyA(),
            request.getCity(), request.getPopulation(), request.getTrafficLevel(), request.getPollutionLevel(), request.getTimeHorizon(),
            request.getBudget(), request.getPriority(), request.getRiskLevel()
        );
        SimulationRequest reqB = new SimulationRequest(
            request.getNeighborhood(), request.getPolicyB(),
            request.getCity(), request.getPopulation(), request.getTrafficLevel(), request.getPollutionLevel(), request.getTimeHorizon(),
            request.getBudget(), request.getPriority(), request.getRiskLevel()
        );
        
        SimulationResponse resA = simulate(reqA);
        SimulationResponse resB = simulate(reqB);
        
        CompareResponse response = new CompareResponse(resA, resB, null);
        
        try {
            String recJson = aiService.getCompareRecommendation(request, resA, resB);
            
            if (recJson != null && !recJson.contains("\"error\"")) {
                com.fasterxml.jackson.databind.JsonNode rootNode = objectMapper.readTree(recJson);
                if (rootNode.has("recommendationSummary")) {
                    response.setRecommendationSummary(rootNode.get("recommendationSummary").asText());
                }
            }
        } catch (Exception e) {
            response.setRecommendationSummary("Recommendation engine offline due to API traffic constraints.");
        }
        
        return response;
    }

    public SimulationResponse simulate(SimulationRequest request) {
        try {
            if (request.getPolicy() == null) request.setPolicy("add_metro");
            if (request.getTimeHorizon() == null) request.setTimeHorizon(1);

            // Call the AI Service with the full request context
            String jsonResult = aiService.getSimulationResult(request);

            // Parse returned JSON string into SimulationResponse object
            if (jsonResult != null && !jsonResult.contains("\"error\"")) {
                return objectMapper.readValue(jsonResult, SimulationResponse.class);
            }
        } catch (Exception e) {
            System.err.println("AI Simulation failed, safely falling back to dummy response: " + e.getMessage());
        }

        // Fallback error handling returning the dummy response footprint
        return getFallbackResponse(request.getPolicy());
    }

    public com.urbanpulse.model.DebateResponse generateDebate(SimulationRequest request) {
        try {
            if (request.getPolicy() == null) request.setPolicy("add_metro");
            if (request.getTimeHorizon() == null) request.setTimeHorizon(1);

            // Call the AI Service debate engine passing the full context
            String jsonResult = aiService.getDebateResult(request);

            if (jsonResult != null && !jsonResult.contains("\"error\"")) {
                return objectMapper.readValue(jsonResult, com.urbanpulse.model.DebateResponse.class);
            }
        } catch (Exception e) {
            System.err.println("AI Debate failed: " + e.getMessage());
        }

        // Return a basic fallback debate if failure occurs
        com.urbanpulse.model.DebateMessage m1 = new com.urbanpulse.model.DebateMessage("Citizen", "We need this change!");
        com.urbanpulse.model.DebateMessage m2 = new com.urbanpulse.model.DebateMessage("Business Owner", "But at what cost to the local economy?");
        com.urbanpulse.model.DebateMessage m3 = new com.urbanpulse.model.DebateMessage("Environmentalist", "We must think of the future implications.");
        return new com.urbanpulse.model.DebateResponse(java.util.Arrays.asList(m1, m2, m3));
    }

    private SimulationResponse getFallbackResponse(String policy) {
        // Fallback impact footprint
        Impact impact = new Impact(50, 50, 50, 50);

        String safePolicy = policy != null ? policy.toLowerCase() : "";

        Stakeholder citizen = new Stakeholder("Citizen", getCitizenReaction(safePolicy));
        Stakeholder businessOwner = new Stakeholder("Business Owner", getBusinessOwnerReaction(safePolicy));
        Stakeholder environmentalist = new Stakeholder("Environmentalist", getEnvironmentalistReaction(safePolicy));

        List<Stakeholder> stakeholders = Arrays.asList(citizen, businessOwner, environmentalist);
        
        com.urbanpulse.model.TimelineEvent ev1 = new com.urbanpulse.model.TimelineEvent(1, "Construction Chaos 😵", "Immediate disruptions and heavy infrastructural costs.");
        com.urbanpulse.model.TimelineEvent ev2 = new com.urbanpulse.model.TimelineEvent(3, "Mixed Results ⚖️", "Early benefits begin to appear despite lingering friction.");
        com.urbanpulse.model.TimelineEvent ev3 = new com.urbanpulse.model.TimelineEvent(5, "Growth 📈", "Continuous compounding structural dividends achieved.");
        com.urbanpulse.model.TimelineEvent ev4 = new com.urbanpulse.model.TimelineEvent(10, "Transformation 🚀", "Complete systemic overhaul leading to permanent improvements.");
        List<com.urbanpulse.model.TimelineEvent> evolution = Arrays.asList(ev1, ev2, ev3, ev4);

        return new SimulationResponse(impact, stakeholders, "Simulation ran via Safe Fallback Mode due to API constraints. Core trade-off cannot be generated dynamically.", evolution);
    }

    private String getCitizenReaction(String policy) {
        if (policy.contains("metro") || policy.contains("transit")) {
            return "This will make my daily commute much faster!";
        } else if (policy.contains("park")) {
            return "A great place for my family to relax on weekends.";
        } else if (policy.contains("parking")) {
            return "Where am I going to park my car now?";
        }
        return "I hope this change improves our quality of life in the neighborhood.";
    }

    private String getBusinessOwnerReaction(String policy) {
        if (policy.contains("metro") || policy.contains("transit")) {
            return "More foot traffic could mean more potential customers!";
        } else if (policy.contains("park")) {
            return "Could bring in some weekend crowds, but might reduce available parking.";
        } else if (policy.contains("parking")) {
            return "Less parking means my customers might go somewhere else!";
        }
        return "As long as it doesn't hurt my revenue, I'm okay with it.";
    }

    private String getEnvironmentalistReaction(String policy) {
        if (policy.contains("metro") || policy.contains("transit")) {
            return "Excellent step towards reducing city carbon emissions!";
        } else if (policy.contains("park")) {
            return "More green spaces are exactly what this concrete jungle needs.";
        } else if (policy.contains("parking")) {
            return "Removing cars from the city center is a major win for air quality.";
        }
        return "We must always ensure such decisions are sustainable in the long run.";
    }
}
