package com.koray.core;



/** Shared rules derived from the level progression. */
public final class GameRules {

    public static final int LEVELS_PER_TIER = 5;

    private GameRules() {}

    public static int tierForLevel(int level) {
        return (level - 1) / LEVELS_PER_TIER;
    }
}


