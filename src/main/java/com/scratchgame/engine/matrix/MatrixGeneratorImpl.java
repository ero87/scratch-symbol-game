package com.scratchgame.engine.matrix;

import com.scratchgame.model.CellProbability;
import com.scratchgame.model.Config;
import com.scratchgame.utli.random.JavaRandomSource;
import com.scratchgame.utli.random.RandomSource;

import java.util.List;
import java.util.Map;

public class MatrixGeneratorImpl implements MatrixGenerator {

    private final RandomSource randomSource;

    public MatrixGeneratorImpl() {
        this.randomSource = new JavaRandomSource();
    }

    public String[][] generateMatrix(Config config) {
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

        int bonusRow = randomSource.nextInt(rows);
        int bonusCol = randomSource.nextInt(cols);
        String bonusSymbol = selectSymbol(config.probabilities().bonusSymbols().symbols());
        matrix[bonusRow][bonusCol] = bonusSymbol;

        return matrix;
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
        int randomValue = randomSource.nextInt(totalWeight);
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
