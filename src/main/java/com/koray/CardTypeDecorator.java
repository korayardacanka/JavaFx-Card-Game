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
        if (effect instanceof PoisonEffect) return "#D8FFD8"; // koyu yeşil
        if (effect instanceof BurnEffect)   return "#FFE0B2"; // turuncu
        if (effect instanceof FreezeEffect) return "#E0F4FF"; // buz mavisi
        return wrapped.getBackground();
    }
 
    public String getBorder() {
        return wrapped.getBorder();
    }
}