package com.koray;
 
public class RewardSystem implements Observer {
 
    Game game;
 
    public RewardSystem(Game game) {
        this.game = game;
    }
 
    @Override
    public void onEvent(GameEvent event) {
 
        if (event instanceof EnemyDeathEvent e) {
            if (e.enemy.isBoss()) {
                game.player.addGold(50);
                game.lastEvent = "💀 BOSS öldürüldü! +50 gold, +20 exp";
            } else {
                int goldGain = 10 + (game.level * 3);
                int expGain  = 5  + game.level;
                game.player.addGold(goldGain);
                game.lastEvent = "Enemy öldürüldü! +" + goldGain + " gold, +" + expGain + " exp";
            }
        }
    }
}
