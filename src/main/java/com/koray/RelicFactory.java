package com.koray;
import java.util.*;
public class RelicFactory {
public static List<RelicItem> bossRelics(int level) {
    List<RelicItem> pool = new ArrayList<>();

    int tier = (level - 1) / 5; // 0, 1, 2 ...

    // Max HP artışı: tier'a göre büyür (25, 35, 45...)
    pool.add(new MaxHpRelic(25 + tier * 10));

    // Pasif heal: tier'a göre büyür (5, 8, 11...)
    pool.add(new PassiveHealRelic(5 + tier * 3));

    // Pasif shield: tier'a göre büyür (4, 7, 10...)
    pool.add(new PassiveShieldRelic(4 + tier * 3));

    // 2-3 tane rastgele sun (tekrar almayı engellemek ileride eklenebilir)
    Collections.shuffle(pool);
    return pool.subList(0, Math.min(3, pool.size()));
}
}
