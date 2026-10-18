package de.lmu.sep.loveletter.model;

import java.util.List;

/** A whole game of Love Letter for 2 to 4 players, made of several rounds. */
public class Game {

  private final List<Player> players;
  private Round currentRound;

  /**
   * Creates a game. No round is started yet.
   *
   * @param players the 2 to 4 players, in turn order
   */
  public Game(List<Player> players) {
    this.players = players;
  }

  public List<Player> getPlayers() {
    return players;
  }

  /**
   * Returns the round that is being played.
   *
   * @return the current round, or null before the first round
   */
  public Round getCurrentRound() {
    return currentRound;
  }

  public void setCurrentRound(Round currentRound) {
    this.currentRound = currentRound;
  }
}
