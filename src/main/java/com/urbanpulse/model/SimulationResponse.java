package com.urbanpulse.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimulationResponse {
    private Impact impact;
    private List<Stakeholder> stakeholders;
    private String tradeoffSummary;
    private List<TimelineEvent> evolution;
}
