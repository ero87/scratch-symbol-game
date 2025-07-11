package com.scratchgame.infrastructure.io;

import com.scratchgame.model.GameResult;

import java.io.IOException;

public interface GameResultPrinter {

    void print(GameResult result) throws IOException;
}
