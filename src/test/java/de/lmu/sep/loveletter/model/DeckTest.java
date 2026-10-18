package de.lmu.sep.loveletter.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Tests that a new deck has exactly the cards of the original game. */
class DeckTest {

  @Test
  void newDeckHasSixteenCards() {
    assertEquals(16, new Deck().getCards().size());
  }

  @Test
  void newDeckHasRightCountOfEachCard() {
    Deck deck = new Deck();

    assertEquals(1, countOf(deck, CardType.PRINCESS));
    assertEquals(1, countOf(deck, CardType.COUNTESS));
    assertEquals(1, countOf(deck, CardType.KING));
    assertEquals(2, countOf(deck, CardType.PRINCE));
    assertEquals(2, countOf(deck, CardType.HANDMAID));
    assertEquals(2, countOf(deck, CardType.BARON));
    assertEquals(2, countOf(deck, CardType.PRIEST));
    assertEquals(5, countOf(deck, CardType.GUARD));
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
