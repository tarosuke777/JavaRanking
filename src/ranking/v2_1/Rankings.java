package ranking.v2_1;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import ranking.v2_1.PlayerScores.PlayerScore;
import ranking.v2_1.Players.Player;

public class Rankings {

  record Ranking(int rank, String playerId, String handleName, int score) {

    String toCsv() {
      return this.rank + "," + this.playerId + "," + this.handleName + "," + this.score;
    }

  }

  private List<Ranking> rankings = new ArrayList<>();

  public Rankings(Players players, PlayerScores playerScores) {

    List<PlayerScore> sortedPlayerScores = playerScores.playerScores().stream()
        .sorted(Comparator.comparing(PlayerScore::score).reversed()
            .thenComparing(Comparator.comparing(PlayerScore::playerId)))
        .collect(Collectors.toList());



    int rank = 0;
    int dispRank = 0;
    int prevScore = 0;

    for (PlayerScore playerScore : sortedPlayerScores) {

      Optional<Player> player = players.player(playerScore.playerId());

      if (player.isEmpty()) {
        continue;
      }

      String handleName = player.get().handleName();

      rank++;
      if (playerScore.score() != prevScore) {
        dispRank = rank;
      }
      if (dispRank > 10) {
        break;
      }


      prevScore = playerScore.score();

      this.rankings
          .add(new Ranking(dispRank, playerScore.playerId(), handleName, playerScore.score()));

    }
  }

  String toCsvWithHeader() {
    String lineFeedCode = "\n";
    StringBuilder sb = new StringBuilder();
    sb.append("rank,player_id,handle_name,score" + lineFeedCode);
    this.rankings.forEach(ranking -> sb.append(ranking.toCsv() + lineFeedCode));
    return sb.toString();
  }

}
