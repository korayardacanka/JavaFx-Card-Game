package com.koray;
import java.util.*;
 
public class Game {
    Player player       = new Player();
    Enemy  enemy        = new Enemy();
    int    level        = 1;
    String lastEvent    = "";
    public EventBus eventBus;
    int    maxEnergy    = 3;
    javafx.stage.Stage shopStage;
 
    List<Card>      currentShopCards  = new ArrayList<>();
    List<RelicItem> currentBossRelics = new ArrayList<>(); // boss sonrası relic listesi
    List<RelicItem> ownedRelics       = new ArrayList<>(); // oyuncunun sahip olduğu relicler
}
