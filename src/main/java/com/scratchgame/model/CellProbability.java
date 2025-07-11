package com.scratchgame.model;

import java.util.Map;

public record CellProbability(int column, int row, Map<String, Integer> symbols){
}
