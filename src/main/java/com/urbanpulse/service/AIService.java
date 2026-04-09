package com.urbanpulse.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.urbanpulse.model.SimulationRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AIService {
    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.key2:}")
    private String apiKey2;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-flash-lite-preview:generateContent?key=";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public AIService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    private String buildEnvironmentContext(SimulationRequest request) {
        StringBuilder context = new StringBuilder();
        if (request.getCity() != null && !request.getCity().trim().isEmpty()) {
            context.append("City: ").append(request.getCity()).append("\n");
            
            if (request.getPopulation() != null) {
                context.append("Population: ").append(request.getPopulation()).append("\n");
            }
            if (request.getTrafficLevel() != null && !request.getTrafficLevel().trim().isEmpty()) {
                context.append("Traffic Level: ").append(request.getTrafficLevel()).append("\n");
            }
            if (request.getPollutionLevel() != null && !request.getPollutionLevel().trim().isEmpty()) {
                context.append("Pollution Level: ").append(request.getPollutionLevel()).append("\n");
            }
        } else {
            // Backward compatibility
            String hood = (request.getNeighborhood() != null && !request.getNeighborhood().trim().isEmpty()) 
                            ? request.getNeighborhood() : "urban_dense";
            context.append("Neighborhood: ").append(hood).append("\n");
        }
        
        // Critical temporal constraint parameter
        int years = request.getTimeHorizon() != null ? request.getTimeHorizon() : 1;
        context.append("Time Horizon to Simulate: EXACTLY ").append(years).append(" Year(s) from today.\n");
        
        if (request.getBudget() != null && !request.getBudget().trim().isEmpty()) {
            context.append("Budget Constraint: ").append(request.getBudget()).append("\n");
        }
        if (request.getPriority() != null && !request.getPriority().trim().isEmpty()) {
            context.append("Policy Priority (Optimization Target): ").append(request.getPriority()).append("\n");
        }
        if (request.getRiskLevel() != null && !request.getRiskLevel().trim().isEmpty()) {
            context.append("Risk Level Authorization: ").append(request.getRiskLevel()).append("\n");
        }
        
        return context.toString();
    }

    public String getSimulationResult(SimulationRequest request) {
        int years = request.getTimeHorizon() != null ? request.getTimeHorizon() : 1;
        
        String prompt = "You are an urban planning simulation engine.\n\n" +
                "STRICT RULES:\n" +
                "* Return ONLY valid JSON\n" +
                "* Predict the reality of the policy EXACTLY after " + years + " Year(s)!\n" +
                "* If 1-3 years: Focus on immediate construction pain, disruption, backlash, or quick wins.\n" +
                "* If 5-10 years: Focus on massive compounding structural changes, gentrification, economic booms, or long-term shifts in traffic patterns.\n" +
                "* Do not add explanation text\n" +
                "* Do not add markdown\n\n" +
                "Format:\n" +
                "{\n" +
                "  \"impact\": {\n" +
                "    \"traffic\": number (0-100),\n" +
                "    \"economy\": number (0-100),\n" +
                "    \"environment\": number (0-100),\n" +
                "    \"sentiment\": number (0-100)\n" +
                "  },\n" +
                "  \"stakeholders\": [\n" +
                "    {\"role\": \"Citizen\", \"reaction\": \"...\"},\n" +
                "    {\"role\": \"Business Owner\", \"reaction\": \"...\"},\n" +
                "    {\"role\": \"Environmentalist\", \"reaction\": \"...\"}\n" +
                "  ],\n" +
                "  \"tradeoffSummary\": \"A concise 2-sentence summary explicitly identifying the core trade-off (e.g., great for environment but hurts economy short-term) while strictly analyzing the given Budget constraint and Risk Level\",\n" +
                "  \"evolution\": [\n" +
                "    {\"year\": 1, \"phase\": \"... 😵\", \"description\": \"...\"},\n" +
                "    {\"year\": 3, \"phase\": \"... ⚖️\", \"description\": \"...\"},\n" +
                "    {\"year\": 5, \"phase\": \"... 📈\", \"description\": \"...\"},\n" +
                "    {\"year\": 10, \"phase\": \"... 🚀\", \"description\": \"...\"}\n" +
                "  ]\n" +
                "}\n\n" +
                "Input:\n" +
                buildEnvironmentContext(request) +
                "Policy: " + request.getPolicy();

        return callGemini(prompt);
    }
    
    public String getDebateResult(SimulationRequest request) {
        int years = request.getTimeHorizon() != null ? request.getTimeHorizon() : 1;
        
        String prompt = "You are simulating a debate between 3 stakeholders in a city:\n\n" +
                "1. Citizen\n" +
                "2. Business Owner\n" +
                "3. Environmentalist\n\n" +
                "Topic:\n" +
                buildEnvironmentContext(request) +
                "Policy: " + request.getPolicy() + "\n\n" +
                "STRICT RULES:\n" +
                "* The debate must focus on the reality of the policy EXACTLY after " + years + " Year(s)!\n" +
                "* In Year 1, they should fight over construction, costs, and disruption.\n" +
                "* In Year 10, they should debate the structural outcome, gentrification, long-term economic shifts, etc.\n" +
                "* Each speaks 2-3 lines\n" +
                "* They respond directly to each other\n" +
                "* Keep it realistic\n\n" +
                "Return ONLY valid JSON:\n" +
                "{\n" +
                "  \"debate\": [\n" +
                "    {\"role\": \"Citizen\", \"message\": \"...\"},\n" +
                "    {\"role\": \"Business Owner\", \"message\": \"...\"},\n" +
                "    {\"role\": \"Environmentalist\", \"message\": \"...\"}\n" +
                "  ]\n" +
                "}";

        return callGemini(prompt);
    }

    public String getCompareRecommendation(com.urbanpulse.model.CompareRequest request, com.urbanpulse.model.SimulationResponse resA, com.urbanpulse.model.SimulationResponse resB) {
        String prompt = "You are an expert urban planning judge.\n\n" +
                "STRICT RULES:\n" +
                "* Return ONLY valid JSON\n" +
                "* Analyze the mathematical impact metrics of Policy A vs Policy B.\n" +
                "* Explicitly recommend ONE policy over the other based heavily on the explicitly requested 'Priority' and 'Risk Level' inputs.\n" +
                "* Assess if the chosen policy is financially appropriate under the explicit 'Budget' constraint.\n" +
                "* Do not add explanation text or markdown.\n\n" +
                "Format:\n" +
                "{\n" +
                "  \"recommendationSummary\": \"Your explicit recommendation judging Policy A vs Policy B based on the user's constraints, dynamically highlighting exactly why mathematically it is the superior strategic choice.\"\n" +
                "}\n\n" +
                "Context Constraints:\n" +
                "Budget: " + (request.getBudget() != null ? request.getBudget() : "Unconstrained") + "\n" +
                "Priority: " + (request.getPriority() != null ? request.getPriority() : "Balanced") + "\n" +
                "Risk Level: " + (request.getRiskLevel() != null ? request.getRiskLevel() : "Balanced") + "\n\n" +
                "Policy A (" + request.getPolicyA() + ") Impact: Traffic=" + resA.getImpact().getTraffic() + 
                ", Economy=" + resA.getImpact().getEconomy() + 
                ", Environment=" + resA.getImpact().getEnvironment() + 
                ", Sentiment=" + resA.getImpact().getSentiment() + "\n" +
                "Policy B (" + request.getPolicyB() + ") Impact: Traffic=" + resB.getImpact().getTraffic() + 
                ", Economy=" + resB.getImpact().getEconomy() + 
                ", Environment=" + resB.getImpact().getEnvironment() + 
                ", Sentiment=" + resB.getImpact().getSentiment();

        return callGemini(prompt);
    }

    private String callGemini(String prompt) {
        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", prompt);

        Map<String, Object> part = new HashMap<>();
        part.put("parts", List.of(textPart));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(part));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        String[] keys = {apiKey, apiKey2};

        for (int i = 0; i < keys.length; i++) {
            String currentKey = keys[i];
            if (currentKey == null || currentKey.trim().isEmpty()) continue;

            try {
                String responseStr = restTemplate.postForObject(GEMINI_API_URL + currentKey, requestEntity, String.class);
                
                // Extract the text part of the Gemini response
                JsonNode rootNode = objectMapper.readTree(responseStr);
                String generatedText = rootNode.path("candidates").get(0)
                                               .path("content")
                                               .path("parts").get(0)
                                               .path("text").asText();
                
                // Extract substring between first '{' and last '}' to strip markdown code blocks
                int startIndex = generatedText.indexOf('{');
                int endIndex = generatedText.lastIndexOf('}');
                
                if (startIndex != -1 && endIndex != -1 && endIndex >= startIndex) {
                    String potentialJson = generatedText.substring(startIndex, endIndex + 1);
                    
                    // Validate JSON by parsing it again
                    JsonNode validatedJson = objectMapper.readTree(potentialJson);
                    
                    // Return clean JSON string
                    return objectMapper.writeValueAsString(validatedJson);
                } else {
                    return "{\"error\": \"No JSON block found in the response.\"}";
                }
            } catch (org.springframework.web.client.HttpClientErrorException.TooManyRequests e) {
                System.err.println("Gemini API Warning: Rate limit exceeded for Key " + (i + 1) + " (429 Too Many Requests).");
                if (i < keys.length - 1) {
                    System.err.println("Automatically switching to backup API key...");
                } else {
                    System.err.println("Both API keys are rate-limited. Switching to secure Fallback Mode. Please wait 10 seconds.");
                    return "{\"error\": \"Rate limit exceeded\"}";
                }
            } catch (Exception e) {
                System.err.println("Gemini API Error: " + e.getMessage());
                return "{\"error\": \"Failed to parse or validate Gemini API response\"}";
            }
        }
        
        return "{\"error\": \"No valid API keys available.\"}";
    }
}
