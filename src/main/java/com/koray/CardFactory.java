package com.koray;
import java.util.*;

public class CardFactory {

    public static int tierForLevel(int level) { return (level - 1) / 5; }
    public static String tierSuffix(int tier)  { return "+".repeat(tier); }
    public static int scaleValue(int base, int tier) { return (int)(base * (1 + tier * 0.5)); }
    public static int scalePrice(int base, int tier) { return (int)(base * (1 + tier * 0.8)); }

    public static Card make(String name, int cost, int price, int level, CardEffect effect) {
        CardDesign design = new LevelDesignDecorator(
            new CardTypeDecorator(new BaseDesign(), effect), level);
        return new Card(name, cost, price, level, effect, design);
    }

    public static Card makeScaled(String baseName, int baseCost, int basePrice,
                                   int baseDamage, int level,
                                   java.util.function.BiFunction<Integer,Integer,CardEffect> effectFn) {
        int tier       = tierForLevel(level);
        String name    = baseName + tierSuffix(tier);
        int cost       = Math.min(baseCost + tier, 6);
        int price      = scalePrice(basePrice, tier);
        int value      = scaleValue(baseDamage, tier);
        return make(name, cost, price, level, effectFn.apply(value, tier));
    }

    public static List<Card> allCardsForLevel(int level) {
        List<Card> all = new ArrayList<>();
        all.add(makeScaled("Damage", 1, 10, 15, level, (v,t) -> new DamageEffect(v)));
        all.add(makeScaled("Shield", 1, 10, 10, level, (v,t) -> new ShieldEffect(v)));
        all.add(makeScaled("Heal",   2, 20, 10, level, (v,t) -> new HealEffect(v)));
        all.add(makeScaled("Poison", 1, 15,  3, level, (v,t) -> new PoisonEffect(v)));
        all.add(makeScaled("Burn",   2, 20,  8, level, (v,t) -> new BurnEffect(v)));
        all.add(make("Freeze", 2, 25, level,new FreezeEffect(1)));
        return all;
    }

    public static Card randomCard() {
        List<Card> pool = allCardsForLevel(1);
        return pool.get((int)(Math.random() * pool.size()));
    }

    public static List<Card> shopCards(int level, Player player) {
        Set<String> owned = new HashSet<>();
        for (Card c : player.deck)    owned.add(c.name);
        for (Card c : player.hand)    owned.add(c.name);
        for (Card c : player.discard) owned.add(c.name);

        // İsme göre tekilleştirme yaparak pool oluştur (duplicate önlemi)
        Map<String, Card> poolMap = new LinkedHashMap<>();

        for (Card c : allCardsForLevel(level)) {
            if (!owned.contains(c.name)) poolMap.put(c.name, c);
        }

        if (level > 1) {
            int prevLevel = Math.max(1, level - 5);
            for (Card c : allCardsForLevel(prevLevel)) {
                if (!owned.contains(c.name)) poolMap.putIfAbsent(c.name, c);
            }
        }

        List<Card> pool = new ArrayList<>(poolMap.values());
        Collections.shuffle(pool);
        return pool.subList(0, Math.min(4, pool.size()));
    }
}