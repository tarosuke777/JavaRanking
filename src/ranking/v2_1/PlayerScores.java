package ranking.v2_1;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class PlayerScores {

  record PlayerScore(String playerId, int score) {

  }

  private List<PlayerScore> playerScores;

  public PlayerScores(Scores scores) {

    List<PlayerScore> playerScores =
        scores.scores().stream().map(score -> new PlayerScore(score.playerId(), score.score()))
            .collect(Collectors.toList());

    Map<String, Optional<PlayerScores.PlayerScore>> playerIdWithMaxScore =
        playerScores.stream().collect(Collectors.groupingBy(PlayerScore::playerId,
            Collectors.maxBy(Comparator.comparing(PlayerScore::score))));

    this.playerScores = playerIdWithMaxScore.entrySet().stream()
        .map(set -> new PlayerScore(set.getKey(), set.getValue().get().score()))
        .collect(Collectors.toList());

  }

  public List<PlayerScore> playerScores() {
    return this.playerScores;
  }

}
