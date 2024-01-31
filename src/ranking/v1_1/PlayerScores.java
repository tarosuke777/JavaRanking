package ranking.v1_1;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class PlayerScores {

  private List<PlayerScore> playerScores;

  PlayerScores(ScoreLogs scoreLogs) {
    Map<String, Integer> playerIdWithSumScore = scoreLogs.getScoreLogs().stream()
        .collect(Collectors.groupingBy(ScoreLog::playerId, Collectors.summingInt(ScoreLog::score)));

    this.playerScores = playerIdWithSumScore.entrySet().stream()
        .map(set -> new PlayerScore(set.getKey(), set.getValue())).collect(Collectors.toList());
  }

  List<PlayerScore> getPlayerScores() {
    return this.playerScores;
  }

}
