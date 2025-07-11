package com.scratchgame.engine.reward;

import com.scratchgame.model.Config;

import java.util.List;
import java.util.Map;

public interface RewardCalculator {

    double calculateReward(Config config,
                           double bettingAmount,
                           Map<String, List<String>> winningCombinations,
                           String[][] matrix);
}
