package com.scratchgame.util.random;

import java.util.Random;

public class JavaRandomSource implements RandomSource {

    private final Random rnd = new Random();

    @Override
    public int nextInt(int bound) {
        return rnd.nextInt(bound);
    }
}
