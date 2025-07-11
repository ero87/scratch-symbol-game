package com.scratchgame.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record WinCombinationConfig(
        @JsonProperty("reward_multiplier") double rewardMultiplier,
        String when,
        Integer count,
        String group,
        @JsonProperty("covered_areas") List<List<String>> coveredAreas
) {
}
