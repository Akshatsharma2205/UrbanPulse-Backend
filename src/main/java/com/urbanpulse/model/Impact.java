package com.urbanpulse.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Impact {
    private int traffic;
    private int economy;
    private int environment;
    private int sentiment;
}
