package com.koray;
import java.util.*;

/**
 * Factory class for creating boss-reward relic pools.
 * After a boss is defeated, this factory builds a randomly shuffled
 * list of 3 relics from the full pool, scaled to the current tier.
 * Already-owned relics are filtered out so the player is never offered
 * a duplicate — the pool shrinks as the player collects more relics.
 *
 * Tier calculation:  tier = (level - 1) / 5
 * Higher tiers yield stronger relic variants (more HP, more heal, etc.).
 */
public class RelicFactory {

    /**
     * Builds a shuffled pool of up to 3 boss relics scaled to the given level.
     * Relics already present in ownedRelics (matched by name) are excluded.
     *
     * @param level       the current game level (used for tier scaling)
     * @param ownedRelics relics the player already owns (used to filter duplicates)
     * @return a list of up to 3 randomly selected, non-owned RelicItem instances
     */
    public static List<RelicItem> bossRelics(int level, List<RelicItem> ownedRelics) {
        int tier = (level - 1) / 5;

        // Build the names of already-owned relics for fast lookup
        Set<String> ownedNames = new HashSet<>();
        for (RelicItem r : ownedRelics) ownedNames.add(r.name);

        List<RelicItem> pool = new ArrayList<>();

        // ── Defensive relics ──────────────────────────────────────────────
        pool.add(new MaxHpRelic(25 + tier * 10));          // max HP boost
        pool.add(new PassiveHealRelic(5 + tier * 3));      // HP regen per turn
        pool.add(new PassiveShieldRelic(4 + tier * 3));    // shield per turn

        // ── Offensive / utility relics ────────────────────────────────────
        pool.add(new ThornRelic(4 + tier * 2));            // reflect damage on hit
        pool.add(new VampireRelic(0.20));                  // 20% lifesteal on damage dealt
        pool.add(new WrathRelic(1));                       // +1 energy when low HP
        pool.add(new GoldRushRelic(3 + tier));             // passive gold per turn
        pool.add(new ExecutionerRelic());                  // bonus damage on low-HP enemies
        pool.add(new BloodPactRelic());                    // high-risk max energy upgrade

        // Filter out already-owned relics
        pool.removeIf(r -> ownedNames.contains(r.name));

        Collections.shuffle(pool);
        return pool.subList(0, Math.min(3, pool.size()));
    }
}