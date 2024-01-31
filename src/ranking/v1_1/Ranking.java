package ranking.v1_1;

record Ranking(int rank, String playerId, int score) {
  String toCsv() {
    return this.rank + "," + this.playerId + "," + this.score;
  }
}
