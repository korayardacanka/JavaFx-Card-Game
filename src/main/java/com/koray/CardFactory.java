package com.koray;
import java.util.*;
import java.util.Random;

/**
 * Factory class for creating and scaling cards.
 *
 * Cards are grouped into tiers based on the current game level
 * (tier 0: levels 1-5, tier 1: levels 6-10, tier 2: levels 11+).
 * Higher tiers yield stronger cards with higher stats and prices.
 * Each card is automatically wrapped in the full design Decorator chain
 * (BaseDesign → CardTypeDecorator → LevelDesignDecorator).
 */
public class CardFactory {

    // ── Tier helpers ──────────────────────────────────────────────────────────

    /** Returns the tier index (0-based) for the given game level. */
    public static int tierForLevel(int level) { return GameRules.tierForLevel(level); }

    /** Returns a "+" suffix string matching the tier (tier 0 → "", tier 2 → "++"). */
    public static String tierSuffix(int tier)  { return "+".repeat(tier); }

    /** Scales a base stat value by 50% per tier. */
    public static int scaleValue(int base, int tier) { return (int)(base * (1 + tier * 0.5)); }

    /** Scales a base price value by 80% per tier. */
    public static int scalePrice(int base, int tier) { return (int)(base * (1 + tier * 0.8)); }

    // ── Builders ──────────────────────────────────────────────────────────────

    /**
     * Creates a single card with the full design Decorator chain applied.
     *
     * @param name   display name
     * @param cost   energy cost
     * @param price  gold price in shop
     * @param level  current game level (used to pick tier and design)
     * @param effect the card's Strategy effect
     */
    public static Card make(String name, int cost, int price, int level, CardEffect effect) {
        CardDesign design = new LevelDesignDecorator(
            new CardTypeDecorator(new BaseDesign(), effect), level);
        return new Card(name, cost, price, effect, design);
    }

    /**
     * Creates a tier-scaled card whose name, cost, price, and effect
     * value all increase automatically with the current game level.
     *
     * @param baseName   base card name (suffix is appended per tier)
     * @param baseCost   energy cost at tier 0
     * @param basePrice  gold price at tier 0
     * @param baseDamage effect magnitude at tier 0
     * @param level      current game level
     * @param effectFn   factory lambda: (scaledValue, tier) → CardEffect
     */
    public static Card makeScaled(String baseName, int baseCost, int basePrice,
                                   int baseDamage, int level,
                                   java.util.function.BiFunction<Integer,Integer,CardEffect> effectFn) {
        int tier    = tierForLevel(level);
        String name = baseName + tierSuffix(tier);
        int cost    = Math.min(baseCost + tier, 6);
        int price   = scalePrice(basePrice, tier);
        int value   = scaleValue(baseDamage, tier);
        return make(name, cost, price, level, effectFn.apply(value, tier));
    }

    // ── Card pool ─────────────────────────────────────────────────────────────

    /**
     * Returns the full pool of all cards scaled for the given level.
     * Used as the source for both shop generation and random picks.
     */
    public static List<Card> allCardsForLevel(int level) {
        List<Card> all = new ArrayList<>(starterCardsForLevel(level));
        all.add(makeScaled("Poison", 1, 15,  3, level, (v,t) -> new PoisonEffect(v)));
        all.add(makeScaled("Burn",   2, 20,  8, level, (v,t) -> new BurnEffect(v)));
        all.add(make("Freeze", 2, 25, level, new FreezeEffect(1)));
        return all;
    }

    /** Creates a fresh, unscaled starter deck with the original card composition. */
    public static List<Card> starterDeck() {
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < UIConstants.INITIAL_DECK_COPIES; i++) {
            deck.addAll(starterCardsForLevel(1));
        }
        return deck;
    }

    private static List<Card> starterCardsForLevel(int level) {
        return List.of(
            makeScaled("Damage", 1, 10, 15, level, (v,t) -> new DamageEffect(v)),
            makeScaled("Shield", 1, 10, 10, level, (v,t) -> new ShieldEffect(v)),
            makeScaled("Heal",   2, 20, 10, level, (v,t) -> new HealEffect(v))
        );
    }

    /**
     * Builds the shop card selection for a given level.
     * Filters out cards the player already owns, then picks up to 4
     * unique cards (deduped by name) from the current and previous tiers.
     *
     * @param level  current game level
     * @param player used to check already-owned cards
     */
    public static List<Card> shopCards(int level, Player player) {
        return shopCards(level, player, new Random());
    }

    public static List<Card> shopCards(int level, Player player, Random random) {
        // Collect names of cards the player already owns across all piles
        Set<String> owned = new HashSet<>();
        for (Card c : player.getDeck())    owned.add(c.name);
        for (Card c : player.getHand())    owned.add(c.name);
        for (Card c : player.getDiscard()) owned.add(c.name);

        // Build a name-keyed map to prevent duplicate entries in the shop
        Map<String, Card> poolMap = new LinkedHashMap<>();

        for (Card c : allCardsForLevel(level)) {
            if (!owned.contains(c.name)) poolMap.put(c.name, c);
        }

        // Also include cards from the previous tier for variety
        if (level > 1) {
            int prevLevel = Math.max(1, level - GameRules.LEVELS_PER_TIER);
            for (Card c : allCardsForLevel(prevLevel)) {
                if (!owned.contains(c.name)) poolMap.putIfAbsent(c.name, c);
            }
        }

        List<Card> pool = new ArrayList<>(poolMap.values());
        Collections.shuffle(pool, random);
        return pool.subList(0, Math.min(4, pool.size()));
    }
}