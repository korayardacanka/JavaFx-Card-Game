package com.koray;
 
/**
 * 😡 Öfke Taşı
 * HP yarının altına düştüğünde her tur +1 enerji kazanır.
 */
class WrathRelic extends RelicItem {
    private int bonusEnergy;
 
    public WrathRelic(int bonusEnergy) {
        super("😡 Öfke Taşı",
              "HP < %50 iken her tur +" + bonusEnergy + " enerji",
              50);
        this.bonusEnergy = bonusEnergy;
    }
 
    @Override
    public void applyPassive(Player player, Game game) {
        if (player.getHp() < player.getMaxHp() / 2) {
            player.restoreEnergy(player.getEnergy() + bonusEnergy);
        }
    }
}