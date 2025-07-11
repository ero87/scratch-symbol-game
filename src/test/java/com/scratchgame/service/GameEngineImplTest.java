package com.scratchgame.service;

import com.scratchgame.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GameEngineImplTest {

    private GameEngineImpl gameEngine;

    @BeforeEach
    void setup() {
        // Define standard symbols A and B
        Map<String, SymbolConfig> symbols = new HashMap<>();
        symbols.put("A", new SymbolConfig("standard", 2.0, null, null));
        symbols.put("B", new SymbolConfig("standard", 1.5, null, null));
        symbols.put("+1000", new SymbolConfig("bonus", 2.0, null, "multiply_reward"));
        symbols.put("MISS", new SymbolConfig("bonus", 0.0, null, null));

        // Define standard symbol weights (used in all cells)
        Map<String, Integer> standardSymbolWeights = new HashMap<>();
        standardSymbolWeights.put("A", 50);
        standardSymbolWeights.put("B", 50);
        List<CellProbability> standardSymbols = List.of(
                new CellProbability(0, 0, standardSymbolWeights),
                new CellProbability(0, 1, standardSymbolWeights),
                new CellProbability(0, 2, standardSymbolWeights),
                new CellProbability(1, 0, standardSymbolWeights),
                new CellProbability(1, 1, standardSymbolWeights),
                new CellProbability(1, 2, standardSymbolWeights),
                new CellProbability(2, 0, standardSymbolWeights),
                new CellProbability(2, 1, standardSymbolWeights),
                new CellProbability(2, 2, standardSymbolWeights)
        );

        // Define bonus symbol weights
        Map<String, Integer> bonusSymbolWeights = new HashMap<>();
        bonusSymbolWeights.put("+1000", 1);
        BonusProbability bonusProbability = new BonusProbability(bonusSymbolWeights);

        Probabilities probabilities = new Probabilities(standardSymbols, bonusProbability);

        // Define win combinations
        Map<String, WinCombinationConfig> winCombinations = new HashMap<>();
        winCombinations.put("same_symbol_3_times", new WinCombinationConfig(
                1.2,
                "same_symbols",
                3,
                "grp1",
                null
        ));
        winCombinations.put("same_symbol_4_times", new WinCombinationConfig(
                1.5,
                "same_symbols",
                4,
                "grp2",
                null
        ));

        Config config = new Config(3, 3, symbols, probabilities, winCombinations);
        gameEngine = new GameEngineImpl(config);
    }

    @Test
    void testPlayGameReturnsValidMatrix() {
        double bet = 100.0;
        GameResult result = gameEngine.playGame(bet);

        assertNotNull(result);
        assertEquals(3, result.matrix().length);
        assertEquals(3, result.matrix()[0].length);
        assertNotNull(result.appliedWinningCombinations());
    }

    @Test
    void testRewardNonNegative() {
        double bet = 100.0;
        GameResult result = gameEngine.playGame(bet);
        assertTrue(result.reward() >= 0.0, "Reward should not be negative");
    }

    @Test
    void testBonusAppliedIfWinExists() {
        GameResult result = gameEngine.playGame(100.0);
        if (!result.appliedWinningCombinations().isEmpty()) {
            String bonus = result.appliedBonusSymbol();
            assertTrue(bonus == null || bonus.startsWith("+"), "Bonus symbol should apply only if win exists");
        }
    }

    @Test
    void testRewardIsZeroWhenNoWinningCombinations() {
        Map<String, SymbolConfig> symbols = new HashMap<>();
        symbols.put("Z", new SymbolConfig("standard", 1.0, null, null));

        Map<String, Integer> onlyZ = new HashMap<>();
        onlyZ.put("Z", 100);

        List<CellProbability> stdSymbols = List.of(
                new CellProbability(0, 0, onlyZ),
                new CellProbability(0, 1, onlyZ),
                new CellProbability(0, 2, onlyZ),
                new CellProbability(1, 0, onlyZ),
                new CellProbability(1, 1, onlyZ),
                new CellProbability(1, 2, onlyZ),
                new CellProbability(2, 0, onlyZ),
                new CellProbability(2, 1, onlyZ),
                new CellProbability(2, 2, onlyZ)
        );

        BonusProbability bonusProbability = new BonusProbability(Map.of("MISS", 1));
        Probabilities probabilities = new Probabilities(stdSymbols, bonusProbability);

        Config config = new Config(3, 3, symbols, probabilities, Collections.emptyMap());
        GameEngineImpl noWinEngine = new GameEngineImpl(config);

        GameResult result = noWinEngine.playGame(100.0);
        assertEquals(0.0, result.reward());
        assertTrue(result.appliedWinningCombinations().isEmpty());
    }
}
