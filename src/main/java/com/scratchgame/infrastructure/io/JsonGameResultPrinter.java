package com.scratchgame.infrastructure.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scratchgame.model.GameResult;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class JsonGameResultPrinter implements GameResultPrinter {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void print(GameResult result) throws IOException {
        Map<String, Object> output = new LinkedHashMap<>();
        output.put("matrix", result.matrix());
        output.put("reward", result.reward());

        if (result.reward() > 0) {
            output.put("appliedWinningCombinations", result.appliedWinningCombinations());
            output.put("appliedBonusSymbol", result.appliedBonusSymbol());
        }

        String jsonOutput = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(output);
        System.out.println(jsonOutput);
    }
}
