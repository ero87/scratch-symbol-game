package com.scratchgame.engine;

import com.scratchgame.engine.combination.CombinationMatcher;
import com.scratchgame.engine.combination.CombinationMatcherImpl;
import com.scratchgame.engine.matrix.MatrixGenerator;
import com.scratchgame.engine.matrix.MatrixGeneratorImpl;
import com.scratchgame.engine.reward.RewardCalculator;
import com.scratchgame.engine.reward.RewardCalculatorImpl;
import com.scratchgame.model.*;
import com.scratchgame.utli.BonusSymbolFinder;

import java.util.*;

public class GameEngineImpl implements GameEngine {

    private final Config config;
    private final MatrixGenerator matrixGenerator;
    private final CombinationMatcher combinationMatcher;
    private final RewardCalculator rewardCalculator;

    public GameEngineImpl(Config config) {
        this.config = config;
        this.matrixGenerator = new MatrixGeneratorImpl();
        this.combinationMatcher = new CombinationMatcherImpl();
        this.rewardCalculator = new RewardCalculatorImpl();
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
