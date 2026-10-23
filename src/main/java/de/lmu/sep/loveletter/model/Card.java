package de.lmu.sep.loveletter.model;

import java.util.Objects;

/**
 * One card: its type and its value.
 *
 * <p>For most types the value is the fixed value of the type. Only the Doppelganger gets a value
 * rolled when the deck is built (sheet 2a). Two cards are equal if type and value are equal.
 *
 * @param type the kind of card
 * @param value the value used in every comparison, between {@link CardType#MIN_VALUE} and
 *     {@link CardType#MAX_VALUE}
 */
public record Card(CardType type, int value) {

  /**
   * Checks that the value fits the type.
   *
   * @throws NullPointerException if type is null
   * @throws IllegalArgumentException if a fixed type gets another value than its own, or a random
   *     type gets a value outside 0 to 8
   */
  public Card {
    Objects.requireNonNull(type, "type must not be null");
    if (type.hasRandomValue()) {
      if (value < CardType.MIN_VALUE || value > CardType.MAX_VALUE) {
        throw new IllegalArgumentException(
            type.getName() + " needs a value from " + CardType.MIN_VALUE + " to "
                + CardType.MAX_VALUE + ", got " + value);
      }
    } else if (value != type.getValue()) {
      throw new IllegalArgumentException(
          type.getName() + " has the value " + type.getValue() + ", got " + value);
    }
  }

  /**
   * Creates a card of a type with a fixed value.
   *
   * @param type the kind of card
   * @throws IllegalStateException if the type has a random value
   */
  public Card(CardType type) {
    this(type, type.getValue());
  }

  /**
   * Returns the name shown to players. A random value is part of the name, because it differs
   * from card to card, for example "Doppelganger (5)".
   *
   * @return the card name
   */
  @Override
  public String toString() {
    return type.hasRandomValue() ? type.getName() + " (" + value + ")" : type.getName();
  }
}
