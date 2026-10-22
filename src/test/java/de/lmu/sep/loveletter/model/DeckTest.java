package de.lmu.sep.loveletter.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import org.junit.jupiter.api.Test;

/** Tests the deck: the cards from the original game and sheet 2a, shuffling and drawing. */
class DeckTest {

  @Test
  void newDeckHasThirtyCards() {
    assertEquals(30, new Deck(new Random(42)).getCards().size());
  }

  @Test
  void newDeckHasRightCountOfEachCard() {
    Deck deck = new Deck(new Random(42));

    assertEquals(1, countOf(deck, CardType.PRINCESS));
    assertEquals(1, countOf(deck, CardType.COUNTESS));
    assertEquals(1, countOf(deck, CardType.KING));
    assertEquals(2, countOf(deck, CardType.PRINCE));
    assertEquals(2, countOf(deck, CardType.HANDMAID));
    assertEquals(2, countOf(deck, CardType.BARON));
    assertEquals(2, countOf(deck, CardType.PRIEST));
    assertEquals(5, countOf(deck, CardType.GUARD));
    assertEquals(4, countOf(deck, CardType.BLACK_MARK));
    assertEquals(4, countOf(deck, CardType.MIRROR));
    assertEquals(3, countOf(deck, CardType.OCCULTIST));
    assertEquals(3, countOf(deck, CardType.PHANTOM));
  }

  @Test
  void drawRemovesTopCard() {
    Deck deck = new Deck(new Random(42));
    CardType top = deck.getCards().get(deck.getCards().size() - 1);

    assertEquals(top, deck.draw());
    assertEquals(29, deck.getCards().size());
  }

  @Test
  void drawingAllCardsEmptiesDeck() {
    Deck deck = new Deck(new Random(42));
    for (int i = 0; i < 30; i++) {
      deck.draw();
    }

    assertTrue(deck.isEmpty());
    assertThrows(IllegalStateException.class, deck::draw);
  }

  @Test
  void shuffleChangesOrder() {
    Deck unshuffled = new Deck(new Random(42));
    Deck shuffled = new Deck(new Random(42));
    shuffled.shuffle();

    assertNotEquals(unshuffled.getCards(), shuffled.getCards());
  }

  @Test
  void sameSeedGivesSameOrder() {
    Deck first = new Deck(new Random(42));
    Deck second = new Deck(new Random(42));
    first.shuffle();
    second.shuffle();

    assertEquals(first.getCards(), second.getCards());
  }

  @Test
  void cardsCannotBeChangedFromOutside() {
    Deck deck = new Deck(new Random(42));

    assertThrows(UnsupportedOperationException.class, () -> deck.getCards().clear());
  }

  @Test
  void deckNeedsRandom() {
    assertThrows(NullPointerException.class, () -> new Deck(null));
  }

  private static int countOf(Deck deck, CardType type) {
    int count = 0;
    for (CardType card : deck.getCards()) {
      if (card == type) {
        count++;
      }
    }
    return count;
  }
}
