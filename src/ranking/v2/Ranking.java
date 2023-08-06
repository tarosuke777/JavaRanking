package ranking.v2;

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

    var gameEntryLogPath = Paths.get(args[0]);
    var gameScoreLogPath = Paths.get(args[1]);

    Map<String, String> gameEntryLogData = gameEntryLogData(gameEntryLogPath);
    Map<String, Integer> playerLogData = getPlayLogData(gameScoreLogPath);

    playerLogData = sortPlayLogData(playerLogData);

    List<String> rankingData = getRankingData(playerLogData, gameEntryLogData);

    outputRankingData(rankingData);
  }

  /**
   * playerId毎にスコアを集計したプレイヤーログデータを取得
   * 
   * @param scoreLogFilePath
   * @return プレイヤーログデータ
   */
  private static Map<String, String> gameEntryLogData(Path gameEntryLogPath) throws IOException {
    try (Stream<String> lines = Files.lines(gameEntryLogPath)) {
      return lines.skip(1).map(line -> line.split(","))
          .collect(toMap(values -> values[0], values -> values[1]));
    }
  }

  /**
   * playerId毎にスコアを集計したプレイヤーログデータを取得
   * 
   * @param scoreLogFilePath
   * @return プレイヤーログデータ
   */
  private static Map<String, Integer> getPlayLogData(Path gameScoreLogPath) throws IOException {
    try (Stream<String> lines = Files.lines(gameScoreLogPath)) {
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
  private static List<String> getRankingData(Map<String, Integer> playerLogData,
      Map<String, String> gameEntryLogData) {
    int rank = 0;
    int rankScore = 0;
    List<String> rankingData = new ArrayList<>();
    for (Map.Entry<String, Integer> playlog : playerLogData.entrySet()) {
      if (rankScore == 0 || playlog.getValue() < rankScore) {
        rank += 1;
        rankScore = playlog.getValue();
      }
      if (rank > 10) {
        break;
      }
      rankingData.add(rank + "," + playlog.getKey() + "," + gameEntryLogData.get(playlog.getKey())
          + "," + playlog.getValue());
    }
    return rankingData;
  }

  private static void outputRankingData(List<String> rankingData) throws IOException {
    System.out.println("rank,player_id,handle_name,score");
    rankingData.forEach(line -> System.out.println(line));
  }

}
