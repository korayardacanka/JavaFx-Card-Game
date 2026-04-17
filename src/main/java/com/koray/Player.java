package com.koray;

import java.util.*;

public class Player {
    int hp = 100;
    int shield = 0;
    int gold = 50;
    int exp = 0;
    int energy = 3;
    int maxhp=100;
    List<Card> deck = new ArrayList<>();
    List<Card> hand = new ArrayList<>();
    List<Card> discard = new ArrayList<>();
}
