package ranking.v1_1;

public record Ranking(int rank, String playerId, int score) {
  public String toCsv() {
    return this.rank + "," + this.playerId + "," + this.score;
  }
}
