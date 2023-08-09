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
import java.util.stream.Stream;

public class Ranking {

  public static void main(String[] args) {
    try {
      validateArgs(args);

      Path gameEntryLogPath = Paths.get(args[0]);
      Path gameScoreLogPath = Paths.get(args[1]);

      Map<String, String> gameEntryLogData = gameEntryLogData(gameEntryLogPath);
      Map<String, Integer> playerLogData = getPlayLogData(gameScoreLogPath);

      playerLogData = sortPlayLogData(playerLogData);

      List<String> rankingData = getRankingData(playerLogData, gameEntryLogData);

      outputRankingData(rankingData);

    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }

  }

  /**
   * 実行時引数の妥当性をチェックする
   * 
   * @param args
   */
  private static void validateArgs(String[] args) {
    if (args == null || args.length != 2) {
      System.err.println("invalid args");
      System.exit(1);
    }

    if (Files.notExists(Paths.get(args[0])) || Files.notExists(Paths.get(args[1]))) {
      System.err.println("not exists args File");
      System.exit(1);
    }
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
    int prevScore = 0;
    List<String> rankingData = new ArrayList<>();
    for (Map.Entry<String, Integer> playlog : playerLogData.entrySet()) {
      if (playlog.getValue() != prevScore) {
        rank += 1;
      }
      if (rank > 10) {
        break;
      }
      rankingData.add(rank + "," + playlog.getKey() + "," + gameEntryLogData.get(playlog.getKey())
          + "," + playlog.getValue());
      prevScore = playlog.getValue();
    }
    return rankingData;
  }

  private static void outputRankingData(List<String> rankingData) throws IOException {
    String lineFeedCode = "\n";
    StringBuilder sb = new StringBuilder();
    sb.append("rank,player_id,handle_name,score" + lineFeedCode);
    rankingData.forEach(line -> sb.append(line + lineFeedCode));
    System.out.print(sb.toString());
  }

}
