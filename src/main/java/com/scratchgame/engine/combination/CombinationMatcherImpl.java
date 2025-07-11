package com.scratchgame.engine.combination;

import com.scratchgame.model.Config;
import com.scratchgame.model.SymbolConfig;
import com.scratchgame.model.WinCombinationConfig;

import java.util.*;

public class CombinationMatcherImpl implements CombinationMatcher {

    @Override
    public Map<String, List<String>> findWinningCombinations(Config config, String[][] matrix) {
        Map<String, Integer> symbolCounts = countStandardSymbols(config, matrix);
        Map<String, List<String>> winningCombinations = new HashMap<>();
        Map<String, Set<String>> groupsAppliedPerSymbol = new HashMap<>();

        applyCountBasedWins(config, symbolCounts, winningCombinations, groupsAppliedPerSymbol);
        applyPatternWins(config, matrix, winningCombinations, groupsAppliedPerSymbol);

        return winningCombinations;
    }

    private Map<String, Integer> countStandardSymbols(Config config, String[][] matrix) {
        Map<String, Integer> counts = new HashMap<>();

        for (int i = 0; i < config.rows(); i++) {
            for (int j = 0; j < config.columns(); j++) {
                String symbol = matrix[i][j];
                SymbolConfig sc = config.symbols().get(symbol);
                if (sc != null && "standard".equals(sc.type())) {
                    counts.merge(symbol, 1, Integer::sum);
                }
            }
        }

        return counts;
    }

    private void applyCountBasedWins(Config config,
                                     Map<String, Integer> symbolCounts,
                                     Map<String, List<String>> winningCombinations,
                                     Map<String, Set<String>> groupsAppliedPerSymbol) {

        config.winCombinations().entrySet().stream()
                .filter(entry -> "same_symbols".equals(entry.getValue().when()))
                .sorted((a, b) -> Integer.compare(b.getValue().count(), a.getValue().count()))
                .forEach(entry -> {
                    String winName = entry.getKey();
                    WinCombinationConfig winConfig = entry.getValue();

                    symbolCounts.entrySet().stream()
                            .filter(e -> e.getValue() >= winConfig.count())
                            .forEach(e -> {
                                String symbol = e.getKey();
                                Set<String> groupsForSymbol = groupsAppliedPerSymbol
                                        .computeIfAbsent(symbol, k -> new HashSet<>());

                                if (!groupsForSymbol.contains(winConfig.group())) {
                                    winningCombinations
                                            .computeIfAbsent(symbol, k -> new ArrayList<>())
                                            .add(winName);
                                    groupsForSymbol.add(winConfig.group());
                                }
                            });
                });
    }

    private void applyPatternWins(Config config,
                                  String[][] matrix,
                                  Map<String, List<String>> winningCombinations,
                                  Map<String, Set<String>> groupsAppliedPerSymbol) {

        config.winCombinations().entrySet().stream()
                .filter(entry -> "linear_symbols".equals(entry.getValue().when()))
                .filter(entry -> entry.getValue().coveredAreas() != null)
                .forEach(entry -> {
                    String winName = entry.getKey();
                    WinCombinationConfig winConfig = entry.getValue();

                    winConfig.coveredAreas().forEach(area -> {
                        if (isValidLinearCombination(config, area, matrix)) {
                            String[] firstCell = area.get(0).split(":");
                            int row = Integer.parseInt(firstCell[0]);
                            int col = Integer.parseInt(firstCell[1]);

                            // Verify the cell exists in matrix bounds
                            if (row >= 0 && row < matrix.length && col >= 0 && col < matrix[0].length) {

                                String matchedSymbol = matrix[row][col];
                                SymbolConfig symbolConfig = config.symbols().get(matchedSymbol);

                                // Only apply to standard symbols
                                if (symbolConfig != null && "standard".equals(symbolConfig.type())) {
                                    Set<String> groupsForSymbol = groupsAppliedPerSymbol
                                            .computeIfAbsent(matchedSymbol, k -> new HashSet<>());

                                    if (!groupsForSymbol.contains(winConfig.group())) {
                                        winningCombinations
                                                .computeIfAbsent(matchedSymbol, k -> new ArrayList<>())
                                                .add(winName);
                                        groupsForSymbol.add(winConfig.group());
                                    }
                                }
                            }
                        }
                    });
                });
    }

    private boolean isValidLinearCombination(Config config, List<String> area, String[][] matrix) {
        if (area == null || area.isEmpty()) {
            return false;
        }

        String[] firstCell = area.get(0).split(":");
        int firstRow = Integer.parseInt(firstCell[0]);
        int firstCol = Integer.parseInt(firstCell[1]);

        // Verify first cell exists and is standard symbol
        if (firstRow >= matrix.length || firstCol >= matrix[0].length) {
            return false;
        }

        String firstSymbol = matrix[firstRow][firstCol];
        SymbolConfig firstSymbolConfig = config.symbols().get(firstSymbol);
        if (firstSymbolConfig == null || !"standard".equals(firstSymbolConfig.type())) {
            return false;
        }

        // Check all cells in area match the first symbol
        for (String cell : area) {
            String[] coords = cell.split(":");
            int row = Integer.parseInt(coords[0]);
            int col = Integer.parseInt(coords[1]);

            String symbol = matrix[Integer.parseInt(coords[0])][Integer.parseInt(coords[1])];
            if (!symbol.equals(firstSymbol) || !"standard".equals(config.symbols().get(symbol).type())) {
                return false;
            }

            // Check bounds
            if (row >= matrix.length || col >= matrix[0].length) {
                return false;
            }

            // Check symbol matches
            if (!firstSymbol.equals(matrix[row][col])) {
                return false;
            }

            // Check symbol type
            SymbolConfig currentConfig = config.symbols().get(matrix[row][col]);
            if (currentConfig == null || !"standard".equals(currentConfig.type())) {
                return false;
            }
        }

        return true;
    }
}
