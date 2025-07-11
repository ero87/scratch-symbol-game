package com.scratchgame;

import com.scratchgame.infrastructure.cli.ArgumentParser;
import com.scratchgame.infrastructure.cli.CommandLineArgumentParser;
import com.scratchgame.infrastructure.io.GameResultPrinter;
import com.scratchgame.infrastructure.io.JsonGameResultPrinter;
import com.scratchgame.service.GameEngine;
import com.scratchgame.service.GameEngineImpl;
import com.scratchgame.model.GameConfiguration;
import com.scratchgame.model.GameResult;

public class ScratchGameApp {

    public static void main(String[] args) {
        try {
            GameConfiguration config = parseArguments(args);
            GameEngine engine = new GameEngineImpl(config.config());
            GameResult result = engine.playGame(config.bettingAmount());

            GameResultPrinter printer = new JsonGameResultPrinter();
            printer.print(result);
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            printUsage();
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            System.exit(1);
        }
    }

    private static GameConfiguration parseArguments(String[] args) {
        ArgumentParser parser = new CommandLineArgumentParser(args);
        return parser.parse();
    }

    private static void printUsage() {
        System.err.println("Usage: java -jar <your-jar-file> --config <config-file> --betting-amount <amount>");
    }
}
