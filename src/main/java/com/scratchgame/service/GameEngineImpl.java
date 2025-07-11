package com.scratchgame.service;

import com.scratchgame.model.*;

import java.util.*;

public class GameEngineImpl implements GameEngine {

    private final Config config;
    private final Random random = new Random();

    public GameEngineImpl(Config config) {
        this.config = config;
    }

    @Override
    public GameResult playGame(double bettingAmount) {
        String[][] matrix = generateMatrix();
        Map<String, List<String>> appliedWinningCombinations = findWinningCombinations(matrix);
        double reward = calculateReward(bettingAmount, appliedWinningCombinations, matrix);

        return new GameResult(
                matrix,
                reward,
                appliedWinningCombinations,
                findAppliedBonusSymbol(matrix, !appliedWinningCombinations.isEmpty())
        );
    }

    private String[][] generateMatrix() {
        int rows = config.rows();
        int cols = config.columns();
        String[][] matrix = new String[rows][cols];
        List<CellProbability> stdSymbols = config.probabilities().standardSymbols();
        CellProbability defaultCellProb = stdSymbols.isEmpty() ? null : stdSymbols.get(0);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                CellProbability cellProb = findCellProbability(stdSymbols, i, j);
                if (cellProb == null) cellProb = defaultCellProb;
                if (cellProb == null) {
                    throw new RuntimeException("No standard symbol probability defined for cell (" + i + "," + j + ")");
                }
                matrix[i][j] = selectSymbol(cellProb.symbols());
            }
        }

        int bonusRow = random.nextInt(rows);
        int bonusCol = random.nextInt(cols);
        String bonusSymbol = selectSymbol(config.probabilities().bonusSymbols().symbols());
        matrix[bonusRow][bonusCol] = bonusSymbol;

        return matrix;
    }

    private Map<String, List<String>> findWinningCombinations(String[][] matrix) {
        Map<String, Integer> symbolCounts = countStandardSymbols(matrix);
        Map<String, List<String>> winningCombinations = new HashMap<>();
        Map<String, Set<String>> groupsAppliedPerSymbol = new HashMap<>();

        applySameSymbolCombinations(symbolCounts, winningCombinations, groupsAppliedPerSymbol);
        applyLinearCombinations(matrix, winningCombinations, groupsAppliedPerSymbol);

        return winningCombinations;
    }

    private Map<String, Integer> countStandardSymbols(String[][] matrix) {
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

    private void applySameSymbolCombinations(Map<String, Integer> symbolCounts,
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

    private void applyLinearCombinations(String[][] matrix,
                                         Map<String, List<String>> winningCombinations,
                                         Map<String, Set<String>> groupsAppliedPerSymbol) {

        config.winCombinations().entrySet().stream()
                .filter(entry -> "linear_symbols".equals(entry.getValue().when()))
                .filter(entry -> entry.getValue().coveredAreas() != null)
                .forEach(entry -> {
                    String winName = entry.getKey();
                    WinCombinationConfig winConfig = entry.getValue();

                    winConfig.coveredAreas().forEach(area -> {
                        if (isValidLinearCombination2(area, matrix)) {
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

    private boolean isValidLinearCombination2(List<String> area, String[][] matrix) {
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

    private double calculateReward(double bettingAmount,
                                   Map<String, List<String>> winningCombinations,
                                   String[][] matrix) {

        if (winningCombinations.isEmpty()) {
            return 0.0;
        }

        double baseReward = winningCombinations.entrySet().stream()
                .mapToDouble(entry -> calculateSymbolReward(bettingAmount, entry.getKey(), entry.getValue()))
                .sum();

        return applyBonusReward(baseReward, matrix);
    }

    private double calculateSymbolReward(double bettingAmount,
                                         String symbol,
                                         List<String> winCombinationNames) {

        SymbolConfig symbolConfig = config.symbols().get(symbol);
        double reward = bettingAmount * symbolConfig.rewardMultiplier();

        return winCombinationNames.stream()
                .map(config.winCombinations()::get)
                .map(WinCombinationConfig::rewardMultiplier)
                .reduce(reward, (acc, multiplier) -> acc * multiplier);
    }

    private double applyBonusReward(double currentReward, String[][] matrix) {
        String bonusSymbol = findAppliedBonusSymbol(matrix, true);

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

    private String findAppliedBonusSymbol(String[][] matrix, boolean hasWinningCombination) {
        if (!hasWinningCombination) {
            return null;
        }

        for (int i = 0; i < config.rows(); i++) {
            for (int j = 0; j < config.columns(); j++) {
                String cell = matrix[i][j];
                SymbolConfig sc = config.symbols().get(cell);
                if (sc != null && "bonus".equals(sc.type()) && !"MISS".equals(cell)) {
                    return cell;
                }
            }
        }
        return null;
    }

    private CellProbability findCellProbability(List<CellProbability> stdSymbols, int row, int col) {
        for (CellProbability cp : stdSymbols) {
            if (cp.row() == row && cp.column() == col) {
                return cp;
            }
        }
        return null;
    }

    private String selectSymbol(Map<String, Integer> symbolWeights) {
        int totalWeight = symbolWeights.values().stream().mapToInt(Integer::intValue).sum();
        int randomValue = random.nextInt(totalWeight);
        int cumulative = 0;
        for (Map.Entry<String, Integer> entry : symbolWeights.entrySet()) {
            cumulative += entry.getValue();
            if (randomValue < cumulative) {
                return entry.getKey();
            }
        }
        return null;
    }
}
