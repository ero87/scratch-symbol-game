package com.scratchgame.infrastructure.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scratchgame.model.Config;
import com.scratchgame.model.GameConfiguration;

import java.io.File;
import java.io.IOException;

public class CommandLineArgumentParser implements ArgumentParser {

    private final String[] args;

    public CommandLineArgumentParser(String[] args) {
        this.args = args;
    }

    @Override
    public GameConfiguration parse() {
        String configPath = null;
        Double bettingAmount = null;

        for (int i = 0; i < args.length; i++) {
            if ("--config".equals(args[i]) && i + 1 < args.length) {
                configPath = args[i + 1];
                i++;
            } else if ("--betting-amount".equals(args[i]) && i + 1 < args.length) {
                try {
                    bettingAmount = Double.parseDouble(args[i + 1]);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Betting amount must be a valid number");
                }
                i++;
            }
        }

        if (configPath == null || bettingAmount == null) {
            throw new IllegalArgumentException("Missing required arguments");
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            Config config = mapper.readValue(new File(configPath), Config.class);
            return new GameConfiguration(config, bettingAmount);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error reading config file: " + e.getMessage());
        }
    }
}
