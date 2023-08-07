package ranking.v1;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingInt;
import static java.util.stream.Collectors.toMap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Ranking {

  public static void main(String[] args) throws IOException {

    var scoreLogFilePath = Paths.get(args[0]);

    Map<String, Integer> playerLogData = getPlayLogData(scoreLogFilePath);

    playerLogData = sortPlayLogData(playerLogData);

    List<String> rankingData = getRankingData(playerLogData);

    outputRankingData(rankingData);
  }

  /**
   * playerId毎にスコアを集計したプレイヤーログデータを取得
   * 
   * @param scoreLogFilePath
   * @return プレイヤーログデータ
   */
  private static Map<String, Integer> getPlayLogData(Path scoreLogFilePath) throws IOException {
    try (Stream<String> lines = Files.lines(scoreLogFilePath)) {
      return lines.skip(1) // header
          .map(line -> line.split(",")).collect(
              groupingBy(values -> values[1], summingInt(values -> Integer.parseInt(values[2]))));
    }
  }

  /**
   * ログデータのソート
   * 
   * @param playerLogData
   * @return スコアの降順でソートした結果
   */
  private static Map<String, Integer> sortPlayLogData(Map<String, Integer> playerLogData) {
    return playerLogData.entrySet().stream()
        .sorted(Map.Entry.<String, Integer>comparingByValue().reversed()
            .thenComparing(Map.Entry.<String, Integer>comparingByKey()))
        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (oldVal, newVal) -> oldVal,
            LinkedHashMap::new));
  }

  /**
   * ランキングデータの取得
   * 
   * @param playerLogData
   * @return
   */
  private static List<String> getRankingData(Map<String, Integer> playerLogData) {
    int rank = 0;
    int rankScore = 0;
    List<String> rankingData = new ArrayList<>();
    for (Map.Entry<String, Integer> sorted : playerLogData.entrySet()) {
      if (rankScore == 0 || sorted.getValue() < rankScore) {
        rank += 1;
        rankScore = sorted.getValue();
      }
      if (rank > 10) {
        break;
      }
      rankingData.add(rank + "," + sorted.getKey() + "," + sorted.getValue());
    }
    return rankingData;
  }

  private static void outputRankingData(List<String> rankingData) throws IOException {
    String lineFeedCode = "\n";
    StringBuilder sb = new StringBuilder();
    sb.append("rank,id,totalScore" + lineFeedCode);
    rankingData.forEach(line -> sb.append(line + lineFeedCode));
    System.out.print(sb.toString());
  }

}
