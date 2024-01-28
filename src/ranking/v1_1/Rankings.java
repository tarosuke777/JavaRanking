package ranking.v1_1;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Rankings {

  private List<Ranking> rankingData = new ArrayList<Ranking>();

  public Rankings(PlayerScores playerScores) {

    List<PlayerScore> playerScoresSorted = playerScores.getPlayerScores().stream()
        .sorted(Comparator.comparing(PlayerScore::getScore).reversed()
            .thenComparing(Comparator.comparing(PlayerScore::getPlayerId)))
        .collect(Collectors.toList());

    int rank = 0;
    int prevScore = 0;

    for (PlayerScore playLog : playerScoresSorted) {

      if (playLog.getScore() != prevScore) {
        rank += 1;
      }
      if (rank > 10) {
        break;
      }

      prevScore = playLog.getScore();

      this.rankingData.add(new Ranking(rank, playLog.getPlayerId(), playLog.getScore()));

    }
  }


  public void toCsvForConsole() {
    String lineFeedCode = "\n";
    StringBuilder sb = new StringBuilder();
    sb.append("rank,id,totalScore" + lineFeedCode);
    this.rankingData.forEach(ranking -> sb.append(ranking.toCsv() + lineFeedCode));
    System.out.print(sb.toString());
  }

}
