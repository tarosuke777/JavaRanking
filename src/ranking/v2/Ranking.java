package ranking.v2;


import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.maxBy;
import static java.util.stream.Collectors.toMap;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class Ranking {

  public static void main(String[] args) {
    try {
      validateArgs(args);

      Path gameEntryLogPath = Paths.get(args[0]);
      Path gameScoreLogPath = Paths.get(args[1]);

      Map<String, String> gameEntryLogData = gameEntryLogData(gameEntryLogPath);
      Map<String, String[]> playerLogData = getPlayerLogData(gameScoreLogPath);

      playerLogData = sortPlayerLogData(playerLogData);

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

    if (Files.notExists(Paths.get(args[0]))) {
      System.err.println("not exists args File " + "args[0]:" + args[0]);
      System.exit(1);
    }

    if (Files.notExists(Paths.get(args[1]))) {
      System.err.println("not exists args File " + "args[1]:" + args[1]);
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
   * @throws IOException
   */
  private static Map<String, String[]> getPlayerLogData(Path gameScoreLogPath) throws IOException {
    try (Stream<String> lines = Files.lines(gameScoreLogPath)) {

      Map<String, Optional<String[]>> scoreData = lines.skip(1) // header
          .map(line -> line.split(",")).collect(groupingBy(values -> values[1],
              maxBy(Comparator.comparingInt(values -> Integer.parseInt(values[2])))));

      return scoreData.entrySet().stream()
          .collect(toMap(Map.Entry::getKey, e -> e.getValue().orElseGet(null)));

    }
  }

  /**
   * playerId毎にスコアを集計したプレイヤーログデータを取得
   * 
   * @param scoreLogFilePath
   * @return プレイヤーログデータ
   * @throws IOException
   */
  private static Map<String, String[]> sortPlayerLogData(Map<String, String[]> playerLogData) {

    Comparator<Map.Entry<String, String[]>> valueComparator = Map.Entry.comparingByValue(
        Comparator.comparing(values -> Integer.valueOf(values[2]), Comparator.reverseOrder()));
    Comparator<Map.Entry<String, String[]>> keyComparator = Map.Entry.comparingByKey();

    return playerLogData.entrySet().stream().sorted(valueComparator.thenComparing(keyComparator))
        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (oldVal, newVal) -> oldVal,
            LinkedHashMap::new));

  }

  /**
   * ランキングデータを取得
   * 
   * @param playerLogData
   * @return ランキングデータ
   */
  private static List<String> getRankingData(Map<String, String[]> playerLogData,
      Map<String, String> gameEntryLogData) {
    int prevScore = 0;
    int rank = 0;
    int outRank = 0;
    List<String> rankingData = new ArrayList<>();
    for (Map.Entry<String, String[]> playlog : playerLogData.entrySet()) {

      var playerId = playlog.getKey();
      var handleName = gameEntryLogData.get(playlog.getKey());
      var score = Integer.valueOf(playlog.getValue()[2]);

      if (handleName == null) {
        continue;
      }

      rank += 1;
      if (score != prevScore) {
        outRank = rank;
      }

      if (outRank > 10) {
        break;
      }

      rankingData.add(outRank + "," + playerId + "," + handleName + "," + score);
      prevScore = score;
    }
    return rankingData;

  }

  /**
   * ランキングデータを出力
   * 
   * @param rankingData
   * @throws IOException
   */
  private static void outputRankingData(List<String> rankingData) throws IOException {
    String lineFeedCode = "\n";
    StringBuilder sb = new StringBuilder();
    sb.append("rank,player_id,handle_name,score" + lineFeedCode);
    rankingData.forEach(line -> sb.append(line + lineFeedCode));
    System.out.print(sb.toString());
  }

}
