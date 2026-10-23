package de.lmu.sep.loveletter.model;

/**
 * The kinds of cards in the game, with value and how often each is in the deck.
 *
 * <p>The first eight are the original Love Letter cards, the rest are the custom cards from
 * exercise sheet 2a. All types have a fixed value, except the Doppelganger: its value is rolled
 * for each card when the deck is built, so it is stored in {@link Card}.
 */
public enum CardType {
  PRINCESS("Princess", 8, 1),
  COUNTESS("Countess", 7, 1),
  KING("King", 6, 1),
  PRINCE("Prince", 5, 2),
  HANDMAID("Handmaid", 4, 2),
  BARON("Baron", 3, 2),
  PRIEST("Priest", 2, 2),
  GUARD("Guard", 1, 5),
  BLACK_MARK("Black Mark", 5, 4),
  MIRROR("Mirror", 4, 4),
  OCCULTIST("Occultist", 3, 3),
  PHANTOM("Phantom", 0, 3),
  // No value here: sheet 2a gives every Doppelganger card its own random value (0 to 8), so
  // both copies can differ. The value is rolled in Deck and stored in Card, not in the type.
  DOPPELGANGER("Doppelganger", 2);

  /** The lowest value a card can have. */
  public static final int MIN_VALUE = 0;

  /** The highest value a card can have. */
  public static final int MAX_VALUE = 8;

  private final String name;
  private final int value;
  // true only for the Doppelganger. An enum constant exists once for all cards of its type, so
  // it cannot hold a different value per card; this flag tells Deck and Card to treat it apart.
  private final boolean randomValue;
  private final int countInDeck;

  /** Creates a type with a fixed value. */
  CardType(String name, int value, int countInDeck) {
    this.name = name;
    this.value = value;
    this.randomValue = false;
    this.countInDeck = countInDeck;
  }

  /** Creates a type whose value is rolled for each card. */
  CardType(String name, int countInDeck) {
    this.name = name;
    // Placeholder only, never read: getValue() refuses to return it.
    this.value = 0;
    this.randomValue = true;
    this.countInDeck = countInDeck;
  }

  /**
   * Returns the name printed on the card, for example "Princess".
   * Not the same as {@link #name()}, which returns the constant, for example "PRINCESS".
   *
   * @return the card name
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the fixed value of this type.
   *
   * @return the value, between {@link #MIN_VALUE} and {@link #MAX_VALUE}
   * @throws IllegalStateException if this type has a random value; use {@link Card#value()}
   */
  public int getValue() {
    if (randomValue) {
      // Fail loudly instead of returning the placeholder: a wrong value would silently break
      // every comparison (Baron, Guard, round end).
      throw new IllegalStateException(name + " has no fixed value, use Card.value()");
    }
    return value;
  }

  /**
   * Tells whether the value is rolled for each card instead of being fixed for the type.
   *
   * @return true for the Doppelganger, false for all other types
   */
  public boolean hasRandomValue() {
    return randomValue;
  }

  public int getCountInDeck() {
    return countInDeck;
  }
}
