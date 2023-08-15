package ranking.v2;


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
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Ranking {

  public static void main(String[] args) {

    try {
      validateArgs(args);
    } catch (IllegalArgumentException e) {
      System.err.println(e.getMessage());
      System.exit(1);
    }

    try {
      Path gameEntryLogPath = Paths.get(args[0]);
      Path gameScoreLogPath = Paths.get(args[1]);

      Map<String, String> gameEntryLogData = gameEntryLogData(gameEntryLogPath);
      Map<String, String[]> playerLogData = getPlayerLogData(gameScoreLogPath);

      Map<String, String[]> playerLogSortedData = sortPlayerLogData(playerLogData);

      List<String> rankingData = getRankingData(playerLogSortedData, gameEntryLogData);

      outputRankingData(rankingData);

    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }

  }

  /**
   * 実行時引数の妥当性をチェック
   * 
   * @param args
   */
  private static void validateArgs(String[] args) {
    if (args == null || args.length != 2) {
      throw new IllegalArgumentException("invalid args");
    }

    if (Files.notExists(Paths.get(args[0]))) {
      throw new IllegalArgumentException("not exists args File " + "args[0]:" + args[0]);
    }

    if (Files.notExists(Paths.get(args[1]))) {
      throw new IllegalArgumentException("not exists args File " + "args[1]:" + args[1]);
    }
  }

  /**
   * エントリーログデータを取得
   * 
   * @param gameEntryLogPath
   * @return エントリーログデータ
   * @throws IOException
   */
  private static Map<String, String> gameEntryLogData(Path gameEntryLogPath) throws IOException {
    try (Stream<String> lines = Files.lines(gameEntryLogPath)) {
      return lines.skip(1).map(line -> line.split(","))
          .collect(toMap(values -> values[0], values -> values[1]));
    }
  }

  /**
   * プレイヤーログデータを取得
   * 
   * @param gameScoreLogPath
   * @return プレイヤーログデータ
   * @throws IOException
   */
  private static Map<String, String[]> getPlayerLogData(Path gameScoreLogPath) throws IOException {
    try (Stream<String> lines = Files.lines(gameScoreLogPath)) {
      return lines.skip(1).map(line -> line.split(","))
          .collect(Collectors.toMap(values -> values[1], Function.identity(),
              BinaryOperator.maxBy(Comparator.comparingInt(values -> Integer.valueOf(values[2])))));
    }
  }

  /**
   * プレイヤーログデータをソート
   * 
   * @param playerLogData
   * @return プレイヤーログデータ
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
   */
  private static void outputRankingData(List<String> rankingData) {
    String lineFeedCode = "\n";
    StringBuilder sb = new StringBuilder();
    sb.append("rank,player_id,handle_name,score" + lineFeedCode);
    rankingData.forEach(line -> sb.append(line + lineFeedCode));
    System.out.print(sb.toString());
  }

}
