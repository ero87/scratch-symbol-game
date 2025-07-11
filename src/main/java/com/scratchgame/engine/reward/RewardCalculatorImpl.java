package com.scratchgame.engine.reward;

import com.scratchgame.model.Config;
import com.scratchgame.model.SymbolConfig;
import com.scratchgame.model.WinCombinationConfig;
import com.scratchgame.utli.BonusSymbolFinder;

import java.util.List;
import java.util.Map;

public class RewardCalculatorImpl implements RewardCalculator {

    @Override
    public double calculateReward(Config config,
                                  double bettingAmount,
                                  Map<String, List<String>> winningCombinations,
                                  String[][] matrix) {

        if (winningCombinations.isEmpty()) {
            return 0.0;
        }

        double baseReward = winningCombinations.entrySet().stream()
                .mapToDouble(entry -> calculateSymbolReward(config, bettingAmount, entry.getKey(), entry.getValue()))
                .sum();

        return applyBonusReward(config, baseReward, matrix);
    }

    private double calculateSymbolReward(Config config,
                                         double bettingAmount,
                                         String symbol,
                                         List<String> winCombinationNames) {

        SymbolConfig symbolConfig = config.symbols().get(symbol);
        double reward = bettingAmount * symbolConfig.rewardMultiplier();

        return winCombinationNames.stream()
                .map(config.winCombinations()::get)
                .map(WinCombinationConfig::rewardMultiplier)
                .reduce(reward, (acc, multiplier) -> acc * multiplier);
    }

    private double applyBonusReward(Config config, double currentReward, String[][] matrix) {
        String bonusSymbol = BonusSymbolFinder.find(config, matrix, true);

        if (bonusSymbol == null) {
            return currentReward;
        }

        SymbolConfig sc = config.symbols().get(bonusSymbol);
        if ("multiply_reward".equals(sc.impact())) {
            return currentReward * sc.rewardMultiplier();
        } else if ("extra_bonus".equals(sc.impact())) {
            return currentReward + sc.extra();
        }
        return currentReward;
    }
}
