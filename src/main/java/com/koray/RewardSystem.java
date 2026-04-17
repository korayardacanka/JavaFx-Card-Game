package com.koray;
 
public class RewardSystem implements Observer {
 
    Game game;
 
    public RewardSystem(Game game) {
        this.game = game;
    }
 
    @Override
    public void onEvent(GameEvent event) {
 
        if (event instanceof EnemyDeathEvent e) {
            if (e.enemy.isBoss) {
                // Boss öldürünce büyük ödül
                game.player.gold += 50;
                game.player.exp  += 20;
                game.lastEvent = "💀 BOSS öldürüldü! +50 gold, +20 exp";
            } else {
                // Normal düşman ödülü (level'a göre ölçeklenir)
                int goldGain = 10 + (game.level * 3);
                int expGain  = 5  + game.level;
                game.player.gold += goldGain;
                game.player.exp  += expGain;
                game.lastEvent = "Enemy öldürüldü! +" + goldGain + " gold, +" + expGain + " exp";
            }
        }
    }
}
