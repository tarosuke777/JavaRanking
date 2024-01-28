package ranking.v1_1;

public class Ranking {

  private int rank;
  private String playerId;
  private int score;

  public Ranking(int rank, String playerId, int score) {
    this.rank = rank;
    this.playerId = playerId;
    this.score = score;
  }

  public int getRank() {
    return this.rank;
  }

  public String getPlayerId() {
    return this.playerId;
  }

  public int getScore() {
    return this.score;
  }

  public String toCsv() {
    return this.rank + "," + this.playerId + "," + this.score;
  }

}
