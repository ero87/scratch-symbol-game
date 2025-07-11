package com.scratchgame.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public record Config(
        int columns,
        int rows,
        Map<String, SymbolConfig> symbols,
        Probabilities probabilities,
        @JsonProperty("win_combinations") Map<String, WinCombinationConfig> winCombinations
) {
    public Config {
        // Default values for columns and rows
        columns = columns == 0 ? 3 : columns;
        rows = rows == 0 ? 3 : rows;

        // Defensive copies if needed
        symbols = symbols != null ? Map.copyOf(symbols) : Map.of();
        winCombinations = winCombinations != null ? Map.copyOf(winCombinations) : Map.of();
    }
}
