package de.lmu.sep.loveletter.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

/** Tests the deck: the cards from the original game and sheet 2a, shuffling and drawing. */
class DeckTest {

  @Test
  void newDeckHasThirtyTwoCards() {
    assertEquals(32, new Deck(new Random(42)).getCards().size());
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
    assertEquals(2, countOf(deck, CardType.DOPPELGANGER));
  }

  @Test
  void fixedTypesKeepTheirValue() {
    for (Card card : new Deck(new Random(42)).getCards()) {
      if (!card.type().hasRandomValue()) {
        assertEquals(card.type().getValue(), card.value(), card.toString());
      }
    }
  }

  @Test
  void doppelgangerValuesAreFromZeroToEight() {
    for (int seed = 0; seed < 100; seed++) {
      for (int value : doppelgangerValues(new Deck(new Random(seed)))) {
        assertTrue(value >= 0 && value <= 8, "seed " + seed + " gave " + value);
      }
    }
  }

  @Test
  void doppelgangerValuesCoverZeroAndEight() {
    List<Integer> seen = new ArrayList<>();
    for (int seed = 0; seed < 100; seed++) {
      seen.addAll(doppelgangerValues(new Deck(new Random(seed))));
    }

    assertTrue(seen.contains(0), "0 never rolled");
    assertTrue(seen.contains(8), "8 never rolled");
  }

  @Test
  void sameSeedGivesSameDoppelgangerValues() {
    assertEquals(
        doppelgangerValues(new Deck(new Random(7))), doppelgangerValues(new Deck(new Random(7))));
  }

  @Test
  void drawRemovesTopCard() {
    Deck deck = new Deck(new Random(42));
    Card top = deck.getCards().get(deck.getCards().size() - 1);

    assertEquals(top, deck.draw());
    assertEquals(31, deck.getCards().size());
  }

  @Test
  void drawingAllCardsEmptiesDeck() {
    Deck deck = new Deck(new Random(42));
    for (int i = 0; i < 32; i++) {
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
  void stackedDeckDrawsInGivenOrder() {
    Card guard = new Card(CardType.GUARD);
    Card princess = new Card(CardType.PRINCESS);
    Deck deck = new Deck(List.of(guard, princess));

    assertEquals(princess, deck.draw());
    assertEquals(guard, deck.draw());
    assertTrue(deck.isEmpty());
  }

  @Test
  void stackedDeckCopiesTheList() {
    List<Card> cards = new ArrayList<>(List.of(new Card(CardType.GUARD)));
    Deck deck = new Deck(cards);
    cards.clear();

    assertEquals(1, deck.getCards().size());
  }

  @Test
  void cardsCannotBeChangedFromOutside() {
    Deck deck = new Deck(new Random(42));

    assertThrows(UnsupportedOperationException.class, () -> deck.getCards().clear());
  }

  @Test
  void deckNeedsRandom() {
    assertThrows(NullPointerException.class, () -> new Deck((Random) null));
  }

  @Test
  void stackedDeckNeedsCards() {
    assertThrows(NullPointerException.class, () -> new Deck((List<Card>) null));
  }

  private static int countOf(Deck deck, CardType type) {
    int count = 0;
    for (Card card : deck.getCards()) {
      if (card.type() == type) {
        count++;
      }
    }
    return count;
  }

  private static List<Integer> doppelgangerValues(Deck deck) {
    List<Integer> values = new ArrayList<>();
    for (Card card : deck.getCards()) {
      if (card.type() == CardType.DOPPELGANGER) {
        values.add(card.value());
      }
    }
    return values;
  }
}
