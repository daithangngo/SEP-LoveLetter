package de.lmu.sep.loveletter.model;

import java.util.ArrayList;
import java.util.List;

/** One player: the cards in hand, the played cards and the tokens of affection won so far. */
public class Player {

  private final String name;
  private final List<CardType> hand = new ArrayList<>();
  private final List<CardType> discardPile = new ArrayList<>();
  private int tokens;
  private boolean inRound;
  private boolean protectedByHandmaid;

  /**
   * Creates a player with an empty hand, an empty discard pile and no tokens.
   *
   * @param name the player name
   */
  public Player(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public List<CardType> getHand() {
    return hand;
  }

  public List<CardType> getDiscardPile() {
    return discardPile;
  }

  public int getTokens() {
    return tokens;
  }

  public void setTokens(int tokens) {
    this.tokens = tokens;
  }

  /**
   * Tells whether the player is still in the current round, i.e. not knocked out.
   *
   * @return true if the player is still in the round
   */
  public boolean isInRound() {
    return inRound;
  }

  public void setInRound(boolean inRound) {
    this.inRound = inRound;
  }

  /**
   * Tells whether the player played a Handmaid in their last turn and cannot be targeted.
   *
   * @return true if the player is protected
   */
  public boolean isProtectedByHandmaid() {
    return protectedByHandmaid;
  }

  public void setProtectedByHandmaid(boolean protectedByHandmaid) {
    this.protectedByHandmaid = protectedByHandmaid;
  }
}
