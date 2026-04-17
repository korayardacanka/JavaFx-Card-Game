package com.koray;

public class EnemyDeathEvent extends GameEvent {

    public Enemy enemy;

    public EnemyDeathEvent(Enemy enemy) {
        this.enemy = enemy;
    }
}
