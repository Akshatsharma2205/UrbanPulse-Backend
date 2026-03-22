package com.urbanpulse.controller;

import com.urbanpulse.model.SimulationRequest;
import com.urbanpulse.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestController {

    private final AIService aiService;

    @Autowired
    public TestController(AIService aiService) {
        this.aiService = aiService;
    }

    @GetMapping("/test-ai")
    public String testAi() {
        SimulationRequest request = new SimulationRequest();
        request.setNeighborhood("urban_dense");
        request.setPolicy("add_metro");
        
        return aiService.getSimulationResult(request);
    }
}
