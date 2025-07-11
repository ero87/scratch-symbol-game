package com.scratchgame.utli;

import com.scratchgame.model.Config;
import com.scratchgame.model.SymbolConfig;

public class BonusSymbolFinder {

    private BonusSymbolFinder() {
        // Prevent instantiation
    }

    public static String find(Config config, String[][] matrix, boolean hasWinningCombination) {
        if (!hasWinningCombination) {
            return null;
        }

        int rows = config.rows();
        int cols = config.columns();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                String symbol = matrix[r][c];
                SymbolConfig sc = config.symbols().get(symbol);
                if (sc != null && sc.type().equals("bonus") && !"MISS".equals(symbol)) {
                    return symbol; // first valid bonus symbol wins
                }
            }
        }
        return null;
    }
}
