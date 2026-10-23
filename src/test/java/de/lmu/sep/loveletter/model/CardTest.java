package de.lmu.sep.loveletter.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Tests that a card's value always fits its type, and how cards are shown and compared. */
class CardTest {

  @Test
  void fixedTypeTakesItsOwnValue() {
    assertEquals(8, new Card(CardType.PRINCESS).value());
    assertEquals(0, new Card(CardType.PHANTOM).value());
  }

  @Test
  void fixedTypeRejectsOtherValue() {
    assertThrows(IllegalArgumentException.class, () -> new Card(CardType.GUARD, 2));
  }

  @Test
  void doppelgangerAcceptsZeroToEight() {
    assertEquals(0, new Card(CardType.DOPPELGANGER, 0).value());
    assertEquals(8, new Card(CardType.DOPPELGANGER, 8).value());
  }

  @Test
  void doppelgangerRejectsValueOutsideRange() {
    assertThrows(IllegalArgumentException.class, () -> new Card(CardType.DOPPELGANGER, -1));
    assertThrows(IllegalArgumentException.class, () -> new Card(CardType.DOPPELGANGER, 9));
  }

  @Test
  void doppelgangerNeedsAValue() {
    assertThrows(IllegalStateException.class, () -> new Card(CardType.DOPPELGANGER));
  }

  @Test
  void typeIsRequired() {
    assertThrows(NullPointerException.class, () -> new Card(null, 1));
  }

  @Test
  void onlyDoppelgangerHasRandomValue() {
    for (CardType type : CardType.values()) {
      assertEquals(type == CardType.DOPPELGANGER, type.hasRandomValue(), type.getName());
    }
  }

  @Test
  void doppelgangerTypeHasNoFixedValue() {
    assertThrows(IllegalStateException.class, CardType.DOPPELGANGER::getValue);
  }

  @Test
  void nameShowsRandomValueOnly() {
    assertEquals("Black Mark", new Card(CardType.BLACK_MARK).toString());
    assertEquals("Doppelganger (5)", new Card(CardType.DOPPELGANGER, 5).toString());
  }

  @Test
  void cardsWithSameTypeAndValueAreEqual() {
    assertEquals(new Card(CardType.GUARD), new Card(CardType.GUARD));
    assertEquals(new Card(CardType.DOPPELGANGER, 3), new Card(CardType.DOPPELGANGER, 3));
    assertNotEquals(new Card(CardType.DOPPELGANGER, 3), new Card(CardType.DOPPELGANGER, 4));
  }

  @Test
  void typesWithSameValueAreDifferentCards() {
    Card handmaid = new Card(CardType.HANDMAID);
    Card mirror = new Card(CardType.MIRROR);

    assertEquals(handmaid.value(), mirror.value());
    assertFalse(handmaid.equals(mirror));
    assertTrue(handmaid.equals(new Card(CardType.HANDMAID)));
  }
}
