package ranking.v1_1;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import ranking.v1_1.PlayerScores.PlayerScore;

class Rankings {

  record Ranking(int rank, String playerId, int score) {
    String toCsv() {
      return this.rank + "," + this.playerId + "," + this.score;
    }
  }

  private List<Ranking> rankingData = new ArrayList<Ranking>();

  Rankings(PlayerScores playerScores) {

    List<PlayerScore> playerScoresSorted = playerScores.getPlayerScores().stream()
        .sorted(Comparator.comparing(PlayerScore::score).reversed()
            .thenComparing(Comparator.comparing(PlayerScore::playerId)))
        .collect(Collectors.toList());

    int rank = 0;
    int prevScore = 0;

    for (PlayerScore playLog : playerScoresSorted) {

      if (playLog.score() != prevScore) {
        rank += 1;
      }
      if (rank > 10) {
        break;
      }

      prevScore = playLog.score();

      this.rankingData.add(new Ranking(rank, playLog.playerId(), playLog.score()));

    }
  }


  String toCsvWithHeader() {
    String lineFeedCode = "\n";
    StringBuilder sb = new StringBuilder();
    sb.append("rank,id,totalScore" + lineFeedCode);
    this.rankingData.forEach(ranking -> sb.append(ranking.toCsv() + lineFeedCode));
    return sb.toString();
  }

}
