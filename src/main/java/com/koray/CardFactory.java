package com.koray;
import java.util.*;
import java.util.stream.*;
 
public class CardFactory {
 
    // Tier hesapla: 1-5 → tier0 (+), 6-10 → tier1 (++), 11-15 → tier2 (+++) ...
    public static int tierForLevel(int level) {
        return (level - 1) / 5;
    }
 
    // "+" sembollerini tier kadar ekle
    public static String tierSuffix(int tier) {
        return "+".repeat(tier);
    }
 
    // Değeri tier'a göre ölçekle: her tier %50 artış
    public static int scaleValue(int base, int tier) {
        return (int)(base * (1 + tier * 0.5));
    }
 
    // Fiyatı tier'a göre ölçekle
    public static int scalePrice(int base, int tier) {
        return (int)(base * (1 + tier * 0.8));
    }
 
    // Tek bir kart üret (tier otomatik hesaplanır)
    public static Card make(String name, int cost, int price, int level, CardEffect effect) {
        CardDesign design = new LevelDesignDecorator(
            new CardTypeDecorator(new BaseDesign(), effect), level
        );
        return new Card(name, cost, price, level, effect, design);
    }
 
    // Tier'a göre otomatik kart üret
    public static Card makeScaled(String baseName, int baseCost, int basePrice,
                                   int baseDamage, int level,
                                   java.util.function.BiFunction<Integer, Integer, CardEffect> effectFn) {
        int tier       = tierForLevel(level);
        String name    = baseName + tierSuffix(tier);
        int cost       = Math.min(baseCost + tier, 6); // max 6 energy
        int price      = scalePrice(basePrice, tier);
        int value      = scaleValue(baseDamage, tier);
        CardEffect eff = effectFn.apply(value, tier);
        return make(name, cost, price, level, eff);
    }
 
    // Belirli bir level için tüm mevcut kartları üret
    public static List<Card> allCardsForLevel(int level) {
        List<Card> all = new ArrayList<>();
 
        // DAMAGE kartları
        all.add(makeScaled("Damage", 1, 10, 15, level,
            (val, tier) -> new DamageEffect(val)));
 
        // SHIELD kartları
        all.add(makeScaled("Shield", 1, 10, 10, level,
            (val, tier) -> new ShieldEffect(val)));
 
        // HEAL kartları
        all.add(makeScaled("Heal", 2, 20, 10, level,
            (val, tier) -> new HealEffect(val)));
 
        return all;
    }
 
    // Geriye dönük uyumluluk için (startGame'de kullanılıyor)
    public static List<Card> allCards() {
        return allCardsForLevel(1);
    }
 
    public static Card randomCard() {
        List<Card> pool = allCardsForLevel(1);
        return pool.get((int)(Math.random() * pool.size()));
    }
 
    // Shop için: sadece bu levela ait tier kartları, oyuncunun sahip olmadıkları
    public static List<Card> shopCards(int level, Player player) {
        // Oyuncunun elindeki kart isimlerini topla
        Set<String> owned = new HashSet<>();
        for (Card c : player.deck)    owned.add(c.name);
        for (Card c : player.hand)    owned.add(c.name);
        for (Card c : player.discard) owned.add(c.name);
 
        // Bu level'a uygun kartları üret
        List<Card> pool = allCardsForLevel(level).stream()
            .filter(c -> !owned.contains(c.name))
            .collect(Collectors.toList());
 
        // Bir önceki tier kartları da ekle (yeni başlayanlar için)
        if (level > 1) {
            int prevLevel = Math.max(1, level - 5);
            allCardsForLevel(prevLevel).stream()
                .filter(c -> !owned.contains(c.name))
                .forEach(pool::add);
        }
 
        Collections.shuffle(pool);
        return pool.subList(0, Math.min(4, pool.size()));
    }
}
