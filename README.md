# JavaFX Card Game

A turn-based, deck-building card game built with Java 17+, JavaFX, and Maven. Play cards to deal damage, heal, gain shield, and apply status effects; defeat increasingly powerful enemies, earn gold, and improve your deck and relic collection between battles.

[![CI](https://github.com/korayardacanka/JavaFx-Card-Game/actions/workflows/ci.yml/badge.svg)](https://github.com/korayardacanka/JavaFx-Card-Game/actions/workflows/ci.yml)

## Preview

Gameplay backgrounds and combat visuals change as you progress through the enemy families. This preview shows one of the biome backgrounds used in the game:

![Mountain biome background used in the game](src/main/resources/assets/game_background_1.png)

> Add a full gameplay screenshot or short gameplay GIF here when one is available.

## Features

- Turn-based combat with energy-costed cards.
- Six card effects: damage, healing, shield, poison, burn, and freeze.
- A tiered card pool and a shop between battles.
- Relics that grant passive, defensive, and damage-triggered bonuses.
- Reroll your current hand for 10 gold.
- Permanently increase your hand size in the shop: three upgrades add one card
  each and cost 100, 200, and 300 gold (maximum hand size: 7).
- Four enemy families, each with four phases followed by a boss phase.
- Animated player sprites, custom enemy portraits, and biome backgrounds.

## Requirements

- JDK 17 or newer
- Maven

JavaFX dependencies and the JavaFX Maven plugin are declared in `pom.xml`.

## Run

From the project root:

```bash
mvn javafx:run
```

Run the test suite with:

```bash
mvn test
```

## Headless simulation

Run 1,000 games with the default deterministic seed:

```bash
mvn exec:java
```

Pass a game count and seed to customize the run:

```bash
mvn exec:java -Dexec.args="1000 42"
```

The report shows the conditional death rate for each level: deaths at that
level divided by the number of games that reached it. The simple policy favors
damage, uses healing and defense when useful, and buys one affordable card and
the highest-priority affordable boss relic after victories.

## Design patterns

| Pattern | Role in the game | Main classes |
| --- | --- | --- |
| **Strategy** | Cards delegate their behavior to interchangeable effects. | `CardEffect`, `DamageEffect`, `HealEffect`, `ShieldEffect`, `PoisonEffect`, `BurnEffect`, `FreezeEffect` |
| **Decorator** | Card and enemy designs are extended by wrapping a base design with visual decorators. | `CardDesign`, `BaseDesign`, `CardTypeDecorator`, `LevelDesignDecorator`; `EnemyDesign`, `BaseEnemyDesign`, `BossBorderDecorator` |
| **Observer** | Game events are published to subscribers so rewards and UI updates remain decoupled. | `EventBus`, `Observer`, `RewardSystem`, `UIObserver`, `GameEvent` |
| **Factory** | Centralizes creation of level-scaled cards, enemies, and boss-reward relics. | `CardFactory`, `EnemyFactory`, `RelicFactory` |

## Project structure

```text
src/
├── main/
│   ├── java/com/koray/       # Game logic, JavaFX views, and design-pattern implementations
│   └── resources/assets/     # Sprite frames and biome backgrounds
└── test/java/com/koray/      # JUnit tests
```

## Controls

- Select **Play** on a card to use it.
- Select **End Turn** or press **E** to end your turn.
- Select **Reroll Hand (10 Gold)** to discard your hand and draw the same number of cards.
