package com.scratchgame.infrastructure.cli;

import com.scratchgame.model.GameConfiguration;

public interface ArgumentParser {

    GameConfiguration parse();
}
