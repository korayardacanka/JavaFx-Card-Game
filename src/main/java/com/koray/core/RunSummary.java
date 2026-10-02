package com.koray.core;

import java.util.List;

/** Immutable snapshot of the player's progress when a run ends. */
public record RunSummary(int level, int gold, List<String> relics, int deckSize) {

    public RunSummary {
        relics = List.copyOf(relics);
    }
}


