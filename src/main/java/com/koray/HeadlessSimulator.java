package com.koray;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Runs full games without starting JavaFX and reports conditional death rates
 * by level. The default policy favors direct damage, uses defensive cards when
 * helpful, and buys one affordable card and boss relic after each victory.
 */
public final class HeadlessSimulator {

    private static final int DEFAULT_GAME_COUNT = 1_000;
    private static final long DEFAULT_SEED = 42L;
    private static final int MAX_TURNS_PER_ENEMY = 10_000;
    private static final int MAX_LEVEL = 10_000;

    private HeadlessSimulator() {}

    public static void main(String[] args) {
        if (args.length > 2) {
            throw new IllegalArgumentException("Usage: mvn exec:java [-Dexec.args=\"<games> [seed]\"]");
        }

        int gameCount = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_GAME_COUNT;
        long seed = args.length > 1 ? Long.parseLong(args[1]) : DEFAULT_SEED;
        if (gameCount <= 0) {
            throw new IllegalArgumentException("Game count must be greater than zero.");
        }

        Result result = simulate(gameCount, seed);
        printResult(result);
    }

    public static Result simulate(int gameCount, long seed) {
        if (gameCount <= 0) {
            throw new IllegalArgumentException("Game count must be greater than zero.");
        }

        SortedMap<Integer, MutableLevelStats> totals = new TreeMap<>();
        int highestLevel = 1;

        for (int index = 0; index < gameCount; index++) {
            int deathLevel = playGame(seed + index);
            highestLevel = Math.max(highestLevel, deathLevel);
            for (int level = 1; level <= deathLevel; level++) {
                totals.computeIfAbsent(level, ignored -> new MutableLevelStats()).entrants++;
            }
            totals.get(deathLevel).deaths++;
        }

        SortedMap<Integer, LevelStats> levels = new TreeMap<>();
        for (Map.Entry<Integer, MutableLevelStats> entry : totals.entrySet()) {
            MutableLevelStats stats = entry.getValue();
            levels.put(entry.getKey(), new LevelStats(stats.entrants, stats.deaths));
        }
        return new Result(gameCount, seed, levels, highestLevel);
    }

    private static int playGame(long seed) {
        Random random = new Random(seed);
        Game game = new Game();
        game.eventBus = new EventBus();
        game.eventBus.subscribe(new RewardSystem(game));
        game.enemy = EnemyFactory.createEnemy(game.level);

        DeckManager deckManager = new DeckManager(game, random);
        deckManager.resetPlayerDeck();
        deckManager.drawHand();

        int turnsAtLevel = 0;
        while (game.player.isAlive()) {
            if (game.level > MAX_LEVEL || turnsAtLevel > MAX_TURNS_PER_ENEMY) {
                throw new IllegalStateException(
                    "Simulation exceeded safety limit at level " + game.level + ".");
            }

            applyTurnStart(game, deckManager);

            while (game.player.isAlive()) {
                Card card = chooseCard(game);
                if (card == null) {
                    break;
                }
                playCard(game, card);
                if (!game.enemy.isAlive()) {
                    resolveEnemyDeath(game, random);
                    turnsAtLevel = 0;
                }
            }

            if (!game.player.isAlive()) {
                break;
            }

            game.enemy.processStatusEffects();
            if (!game.enemy.isAlive()) {
                resolveEnemyDeath(game, random);
                turnsAtLevel = 0;
                continue;
            }

            int hpBeforeAttack = game.player.getHp();
            game.enemy.attack(game.player);
            int hpDamage = hpBeforeAttack - game.player.getHp();
            if (hpDamage > 0) {
                for (RelicItem relic : game.ownedRelics) {
                    relic.onDamageTaken(game.player, game.enemy, game, hpDamage);
                }
            }

            if (!game.player.isAlive()) {
                break;
            }
            if (!game.enemy.isAlive()) {
                resolveEnemyDeath(game, random);
                turnsAtLevel = 0;
            }

            int cardsToDraw = game.getHandSizeLimit() - game.player.hand.size();
            for (int i = 0; i < cardsToDraw; i++) {
                deckManager.drawSingleCard();
            }
            turnsAtLevel++;
        }
        return game.level;
    }

    private static void applyTurnStart(Game game, DeckManager deckManager) {
        game.player.restoreEnergy(game.maxEnergy);
        for (RelicItem relic : game.ownedRelics) {
            relic.applyPassive(game.player, game);
        }
        int cardsToDraw = game.getHandSizeLimit() - game.player.hand.size();
        for (int i = 0; i < cardsToDraw; i++) {
            deckManager.drawSingleCard();
        }
    }

    private static Card chooseCard(Game game) {
        Card best = null;
        int bestScore = Integer.MIN_VALUE;
        for (Card card : game.player.hand) {
            if (card.cost > game.player.getEnergy()) {
                continue;
            }
            int score = scoreCard(card, game);
            if (score > bestScore) {
                best = card;
                bestScore = score;
            }
        }
        return best;
    }

    private static int scoreCard(Card card, Game game) {
        CardEffect effect = card.effect;
        int score;
        if (effect instanceof DamageEffect damage) {
            score = damage.damage >= game.enemy.getHp() ? 1_000 : 100 + damage.damage;
        } else if (effect instanceof HealEffect heal) {
            int missingHp = game.player.getMaxHp() - game.player.getHp();
            score = missingHp == 0 ? -1_000 : 55 + Math.min(missingHp, heal.heal);
        } else if (effect instanceof ShieldEffect shield) {
            int unblockedAttack = Math.max(0, game.enemy.getAttackDamage() - game.player.getShield());
            score = unblockedAttack == 0 ? 10 : 50 + Math.min(unblockedAttack, shield.shield);
        } else if (effect instanceof PoisonEffect) {
            score = game.enemy.getPoisonStacks() == 0 && game.enemy.getHp() > 30 ? 85 : 15;
        } else if (effect instanceof BurnEffect) {
            score = game.enemy.getHp() > 50 ? 60 : 20;
        } else if (effect instanceof FreezeEffect) {
            score = game.enemy.isFrozen() ? 10 : 65 + game.enemy.getAttackDamage() / 2;
        } else {
            score = 0;
        }
        return score - card.cost;
    }

    private static void playCard(Game game, Card card) {
        if (!game.player.spendEnergy(card.cost)) {
            throw new IllegalStateException("Policy selected a card the player cannot afford.");
        }

        int enemyHpBefore = game.enemy.getHp();
        card.use(game.player, game.enemy);
        game.player.hand.remove(card);
        game.player.discard.add(card);

        int dealtDamage = enemyHpBefore - game.enemy.getHp();
        if (dealtDamage > 0) {
            for (RelicItem relic : game.ownedRelics) {
                relic.onEnemyDamaged(game.player, game.enemy, game, dealtDamage);
            }
        }
    }

    private static void resolveEnemyDeath(Game game, Random random) {
        Enemy defeated = game.enemy;
        game.eventBus.publish(new EnemyDeathEvent(defeated));
        game.level++;
        game.enemy = EnemyFactory.createEnemy(game.level);
        if (game.level % 2 == 0) {
            game.maxEnergy++;
        }

        game.currentShopCards = CardFactory.shopCards(game.level, game.player, random);
        game.currentBossRelics = defeated.isBoss()
            ? RelicFactory.bossRelics(game.level, game.ownedRelics, random)
            : new ArrayList<>();
        applyShopPolicy(game);
    }

    private static void applyShopPolicy(Game game) {
        int upgradeCost = game.getNextHandSizeUpgradeCost();
        if (upgradeCost >= 0 && game.player.getGold() >= upgradeCost) {
            game.purchaseHandSizeUpgrade();
        }

        Card bestCard = null;
        int bestCardScore = Integer.MIN_VALUE;
        for (Card card : game.currentShopCards) {
            if (card.price <= game.player.getGold()) {
                int score = cardScoreForPurchase(card) - card.price / 10;
                if (score > bestCardScore) {
                    bestCard = card;
                    bestCardScore = score;
                }
            }
        }
        if (bestCard != null) {
            game.player.spendGold(bestCard.price);
            game.player.deck.add(bestCard);
        }

        RelicItem bestRelic = null;
        int bestRelicScore = Integer.MIN_VALUE;
        for (RelicItem relic : game.currentBossRelics) {
            if (relic.price > game.player.getGold() || !relic.canPurchase(game)) {
                continue;
            }
            int score = relicScore(relic, game);
            if (score > bestRelicScore) {
                bestRelic = relic;
                bestRelicScore = score;
            }
        }
        if (bestRelic != null && bestRelicScore > 0 && bestRelic.canPurchase(game)
                && game.player.spendGold(bestRelic.price)) {
            bestRelic.applyOnBuy(game.player, game);
            game.ownedRelics.add(bestRelic);
        }
    }

    private static int cardScoreForPurchase(Card card) {
        if (card.effect instanceof DamageEffect damage) return 100 + damage.damage;
        if (card.effect instanceof PoisonEffect) return 90;
        if (card.effect instanceof HealEffect) return 70;
        if (card.effect instanceof ShieldEffect) return 65;
        if (card.effect instanceof BurnEffect) return 60;
        if (card.effect instanceof FreezeEffect) return 55;
        return 0;
    }

    private static int relicScore(RelicItem relic, Game game) {
        if (relic instanceof PassiveHealRelic) return 100;
        if (relic instanceof VampireRelic) return 90;
        if (relic instanceof PassiveShieldRelic) return 80;
        if (relic instanceof MaxHpRelic) return game.player.getHp() < game.player.getMaxHp() ? 85 : 70;
        if (relic instanceof ThornRelic) return 65;
        if (relic instanceof ExecutionerRelic) return 60;
        if (relic instanceof WrathRelic) return 50;
        if (relic instanceof GoldRushRelic) return 40;
        return 0;
    }

    private static void printResult(Result result) {
        System.out.printf(Locale.ROOT, "Games: %d | Seed: %d | Highest level reached: %d%n",
            result.games(), result.seed(), result.highestLevelReached());
        System.out.println("Death rate is conditional: deaths at level / games that reached that level.");
        System.out.printf("%-8s %12s %10s %14s%n", "Level", "Reached", "Deaths", "Death rate");
        for (Map.Entry<Integer, LevelStats> entry : result.levels().entrySet()) {
            LevelStats stats = entry.getValue();
            double rate = 100.0 * stats.deaths() / stats.entrants();
            System.out.printf(Locale.ROOT, "%-8d %12d %10d %13.2f%%%n",
                entry.getKey(), stats.entrants(), stats.deaths(), rate);
        }
    }

    public record LevelStats(int entrants, int deaths) {}

    public record Result(int games, long seed, SortedMap<Integer, LevelStats> levels,
                         int highestLevelReached) {}

    private static final class MutableLevelStats {
        private int entrants;
        private int deaths;
    }
}
