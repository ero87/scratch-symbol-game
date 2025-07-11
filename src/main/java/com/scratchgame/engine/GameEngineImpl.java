package com.scratchgame.engine;

import com.scratchgame.engine.combination.CombinationMatcher;
import com.scratchgame.engine.matrix.MatrixGenerator;
import com.scratchgame.engine.reward.RewardCalculator;
import com.scratchgame.model.Config;
import com.scratchgame.model.GameResult;
import com.scratchgame.util.BonusSymbolFinder;

import java.util.List;
import java.util.Map;

public class GameEngineImpl implements GameEngine {

    private final Config config;
    private final MatrixGenerator matrixGenerator;
    private final CombinationMatcher combinationMatcher;
    private final RewardCalculator rewardCalculator;

    public GameEngineImpl(Config config,
                          MatrixGenerator matrixGenerator,
                          CombinationMatcher combinationMatcher,
                          RewardCalculator rewardCalculator) {
        this.config = config;
        this.matrixGenerator = matrixGenerator;
        this.combinationMatcher = combinationMatcher;
        this.rewardCalculator = rewardCalculator;
    }

    @Override
    public GameResult playGame(double bettingAmount) {
        String[][] matrix = matrixGenerator.generateMatrix(config);
        Map<String, List<String>> wins = combinationMatcher.findWinningCombinations(config, matrix);
        double reward = rewardCalculator.calculateReward(config, bettingAmount, wins, matrix);
        String appliedBonusSymbol = BonusSymbolFinder.find(config, matrix, !wins.isEmpty());

        return new GameResult(matrix, reward, wins, appliedBonusSymbol);
    }
}
