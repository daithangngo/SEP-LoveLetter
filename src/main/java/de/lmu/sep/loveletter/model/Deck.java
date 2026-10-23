package de.lmu.sep.loveletter.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/** The draw pile of one round. The top card is the last card in the list. */
public class Deck {

  private final List<CardType> cards = new ArrayList<>();
  private final Random random;

  /**
   * Creates a full deck with every card of every {@link CardType}, not shuffled yet.
   *
   * @param random the source of randomness for {@link #shuffle()}; tests pass a fixed seed so
   *     the order of the cards is always the same
   */
  public Deck(Random random) {
    this.random = Objects.requireNonNull(random, "random must not be null");
    for (CardType type : CardType.values()) {
      for (int i = 0; i < type.getCountInDeck(); i++) {
        cards.add(type);
      }
    }
  }

  /** Puts the cards into a random order. */
  public void shuffle() {
    Collections.shuffle(cards, random);
  }

  /**
   * Takes the top card off the deck.
   *
   * @return the card that was on top
   * @throws IllegalStateException if the deck is empty
   */
  public CardType draw() {
    if (cards.isEmpty()) {
      throw new IllegalStateException("cannot draw from an empty deck");
    }
    return cards.remove(cards.size() - 1);
  }

  /**
   * Tells whether there are no cards left to draw.
   *
   * @return true if the deck is empty
   */
  public boolean isEmpty() {
    return cards.isEmpty();
  }

  /**
   * Returns the cards left in the deck, bottom first and top last.
   *
   * <p>The list is read-only, so cards can only leave the deck through {@link #draw()}.
   *
   * @return a read-only view of the cards
   */
  public List<CardType> getCards() {
    return Collections.unmodifiableList(cards);
  }
}
