package com.scratchgame.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record Probabilities(
        @JsonProperty("standard_symbols") List<CellProbability> standardSymbols,
        @JsonProperty("bonus_symbols") BonusProbability bonusSymbols
) {
}
