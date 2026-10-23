package de.lmu.sep.loveletter.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/** The draw pile of one round. The top card is the last card in the list. */
public class Deck {

  private final List<Card> cards;
  // Used for shuffling and for the Doppelganger values. Passed in from outside so that tests
  // can use a fixed seed and always get the same order and the same values.
  private final Random random;

  /**
   * Creates a full deck with every card of every {@link CardType}, not shuffled yet. Each
   * Doppelganger gets its own value from 0 to 8.
   *
   * @param random the source of randomness for the Doppelganger values and {@link #shuffle()};
   *     tests pass a fixed seed so values and order are always the same
   */
  public Deck(Random random) {
    this.random = Objects.requireNonNull(random, "random must not be null");
    this.cards = new ArrayList<>();
    for (CardType type : CardType.values()) {
      for (int i = 0; i < type.getCountInDeck(); i++) {
        // A Doppelganger gets its own rolled value, so the two copies can differ (sheet 2a).
        // All other cards take the fixed value of their type.
        cards.add(type.hasRandomValue() ? new Card(type, rollValue()) : new Card(type));
      }
    }
  }

  /**
   * Creates a deck with exactly the given cards in the given order. Meant for tests that need to
   * know which card is drawn next.
   *
   * @param cards the cards, bottom first and top last; the list is copied
   */
  public Deck(List<Card> cards) {
    this.cards = new ArrayList<>(Objects.requireNonNull(cards, "cards must not be null"));
    this.random = new Random();
  }

  /** Rolls a Doppelganger value from 0 to 8, both included. */
  private int rollValue() {
    // nextInt excludes the upper bound, so + 1 makes 8 possible.
    return random.nextInt(CardType.MIN_VALUE, CardType.MAX_VALUE + 1);
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
  public Card draw() {
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
  public List<Card> getCards() {
    return Collections.unmodifiableList(cards);
  }
}
