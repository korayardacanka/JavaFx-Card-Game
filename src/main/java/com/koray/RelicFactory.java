package com.koray;
import java.util.*;
 
public class RelicFactory {
 
    public static List<RelicItem> bossRelics(int level) {
        List<RelicItem> pool = new ArrayList<>();
        int tier = (level - 1) / 5;
 
        // ── Mevcut relicler ─────────────────────────────────
        pool.add(new MaxHpRelic(25 + tier * 10));
        pool.add(new PassiveHealRelic(5 + tier * 3));
        pool.add(new PassiveShieldRelic(4 + tier * 3));
 
        // ── Yeni relicler ────────────────────────────────────
        pool.add(new ThornRelic(4 + tier * 2));        // 4, 6, 8 hasar yansıma
        pool.add(new VampireRelic(0.20));              // %20 lifesteal
        pool.add(new WrathRelic(1));                   // öfke: +1 enerji
        pool.add(new GoldRushRelic(3 + tier));         // 3, 4, 5 gold/tur
        pool.add(new ExecutionerRelic());              // execute bonus
        pool.add(new BloodPactRelic());                // riskli ama güçlü
 
        Collections.shuffle(pool);
        return pool.subList(0, Math.min(3, pool.size()));
    }
}