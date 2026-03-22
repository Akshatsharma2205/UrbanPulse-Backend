package com.urbanpulse.controller;

import com.urbanpulse.model.CompareRequest;
import com.urbanpulse.model.CompareResponse;
import com.urbanpulse.model.SimulationRequest;
import com.urbanpulse.model.SimulationResponse;
import com.urbanpulse.service.SimulationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class SimulationController {

    private final SimulationService simulationService;
    private static final Map<String, Object> savedScenarios = new ConcurrentHashMap<>();

    @Autowired
    public SimulationController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @PostMapping("/save")
    public Map<String, String> saveScenario(@RequestBody Map<String, Object> payload) {
        String id = "UP-" + (10000 + new java.util.Random().nextInt(90000));
        savedScenarios.put(id, payload);
        return Map.of("scenarioId", id);
    }

    @GetMapping("/scenario/{id}")
    public Object getScenario(@PathVariable String id) {
        return savedScenarios.getOrDefault(id, Map.of("error", "Scenario not found"));
    }

    @PostMapping("/simulate")
    public SimulationResponse simulate(@RequestBody SimulationRequest request) {
        return simulationService.simulate(request);
    }

    @PostMapping("/compare")
    public CompareResponse compare(@RequestBody CompareRequest request) {
        return simulationService.compare(request);
    }

    @PostMapping("/debate")
    public com.urbanpulse.model.DebateResponse debate(@RequestBody SimulationRequest request) {
        return simulationService.generateDebate(request);
    }
}
