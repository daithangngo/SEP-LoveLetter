package de.lmu.sep.loveletter.model;

import java.util.ArrayList;
import java.util.List;

/** One round of Love Letter: the cards on the table and whose turn it is. */
public class Round {

  private final Deck deck = new Deck();
  private CardType setAsideCard;
  private final List<CardType> faceUpCards = new ArrayList<>();
  private final List<Player> players;
  private int currentPlayerIndex;

  /**
   * Creates a round with a full deck, no set-aside card and the first player to move.
   *
   * @param players the players of the game, in turn order
   */
  public Round(List<Player> players) {
    this.players = players;
  }

  public Deck getDeck() {
    return deck;
  }

  /**
   * Returns the card put aside face down at the start of the round.
   *
   * @return the set-aside card, or null if none was set aside yet
   */
  public CardType getSetAsideCard() {
    return setAsideCard;
  }

  public void setSetAsideCard(CardType setAsideCard) {
    this.setAsideCard = setAsideCard;
  }

  /**
   * Returns the cards put aside face up. Only used in a game with 2 players.
   *
   * @return the face-up cards, empty with 3 or 4 players
   */
  public List<CardType> getFaceUpCards() {
    return faceUpCards;
  }

  public List<Player> getPlayers() {
    return players;
  }

  public int getCurrentPlayerIndex() {
    return currentPlayerIndex;
  }

  public void setCurrentPlayerIndex(int currentPlayerIndex) {
    this.currentPlayerIndex = currentPlayerIndex;
  }
}
