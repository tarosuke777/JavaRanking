package ranking.v1_1;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PlayerScores {

  private List<PlayerScore> playerScores;

  public PlayerScores(ScoreLogs scoreLogs) {
    Map<String, Integer> playerIdWithSumScore = scoreLogs.getScoreLogs().stream()
        .collect(Collectors.groupingBy(ScoreLog::playerId, Collectors.summingInt(ScoreLog::score)));

    this.playerScores = playerIdWithSumScore.entrySet().stream()
        .map(set -> new PlayerScore(set.getKey(), set.getValue())).collect(Collectors.toList());
  }

  public List<PlayerScore> getPlayerScores() {
    return this.playerScores;
  }

}
