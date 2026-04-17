package com.koray;

public class CardTypeDecorator extends DesignDecorator {
    
    private CardEffect effect;

    public CardTypeDecorator(CardDesign wrapped, CardEffect effect) {
        super(wrapped);
        this.effect = effect;
    }

    public String getBackground() {
        if (effect instanceof DamageEffect) return "#FFCCCC"; // kırmızı
        if (effect instanceof HealEffect)   return "#CCFFCC"; // yeşil
        if (effect instanceof ShieldEffect) return "#CCE5FF"; // mavi
        return wrapped.getBackground();                        // bilinmiyorsa default
    }

    public String getBorder() {
        return wrapped.getBorder(); // border'a dokunma, LevelDecorator halleder
    }
}
