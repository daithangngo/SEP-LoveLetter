package de.lmu.sep.loveletter.model;

import java.util.ArrayList;
import java.util.List;

/** The draw pile of one round. */
public class Deck {

  private final List<CardType> cards = new ArrayList<>();

  /** Creates a full deck with all 16 cards, not shuffled. */
  public Deck() {
    for (CardType type : CardType.values()) {
      for (int i = 0; i < type.getCountInDeck(); i++) {
        cards.add(type);
      }
    }
  }

  public List<CardType> getCards() {
    return cards;
  }
}
