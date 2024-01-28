package ranking.v1_1;

public class PlayerScore {

  private String playerId;

  private int score;

  public PlayerScore(String playerId, int score) {
    this.playerId = playerId;
    this.score = score;
  }

  public String getPlayerId() {
    return this.playerId;
  }

  public int getScore() {
    return this.score;
  }


}
