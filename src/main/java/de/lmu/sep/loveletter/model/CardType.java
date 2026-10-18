package de.lmu.sep.loveletter.model;

/** The eight kinds of cards in Love Letter, with value and how often each is in the deck. */
public enum CardType {
  PRINCESS("Princess", 8, 1),
  COUNTESS("Countess", 7, 1),
  KING("King", 6, 1),
  PRINCE("Prince", 5, 2),
  HANDMAID("Handmaid", 4, 2),
  BARON("Baron", 3, 2),
  PRIEST("Priest", 2, 2),
  GUARD("Guard", 1, 5);

  private final String name;
  private final int value;
  private final int countInDeck;

  CardType(String name, int value, int countInDeck) {
    this.name = name;
    this.value = value;
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

  public int getValue() {
    return value;
  }

  public int getCountInDeck() {
    return countInDeck;
  }
}
