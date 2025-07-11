package com.scratchgame.engine.combination;

import com.scratchgame.model.Config;

import java.util.List;
import java.util.Map;

public interface CombinationMatcher {

    Map<String, List<String>> findWinningCombinations(Config config, String[][] matrix);
}
