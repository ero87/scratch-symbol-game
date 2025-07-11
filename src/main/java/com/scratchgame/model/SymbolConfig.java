package com.scratchgame.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SymbolConfig(
        String type,
        @JsonProperty("reward_multiplier")
        Double rewardMultiplier,
        Integer extra,
        String impact
) {
}
