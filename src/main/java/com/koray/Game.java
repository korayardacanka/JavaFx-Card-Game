package com.koray;

import java.util.*;

public class Game {
    Player player    = new Player();
    Enemy  enemy     = new Enemy();
    int    level     = 1;
    String lastEvent = "";
    public EventBus eventBus;
    int    maxEnergy = 3;

    // Shop UI durumu artık Main'de yönetiliyor — Game, JavaFX'ten bağımsız.

    List<Card>      currentShopCards  = new ArrayList<>();
    List<RelicItem> currentBossRelics = new ArrayList<>();
    List<RelicItem> ownedRelics       = new ArrayList<>();
}