package ranking.v1_1;

import java.time.LocalDate;

public class ScoreLog {

  private LocalDate date;

  private String playerId;

  private int score;

  public ScoreLog(LocalDate date, String playerId, Integer score) {
    this.date = date;
    this.playerId = playerId;
    this.score = score;
  }

  public LocalDate getDate() {
    return this.date;
  }

  public String getPlayerId() {
    return this.playerId;
  }

  public Integer getScore() {
    return this.score;
  }

}
