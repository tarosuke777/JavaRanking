package ranking.v3;


import static java.util.stream.Collectors.toMap;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IntSummaryStatistics;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Ranking {

  private static DateTimeFormatter uuuuMM =
      DateTimeFormatter.ofPattern("uuuuMM").withResolverStyle(ResolverStyle.STRICT);

  private enum OutputKbn {
    userRanking("1"), dateSummary("2");

    private String value;

    private OutputKbn(String value) {
      this.value = value;
    }

    private String getValue() {
      return this.value;
    }

    public static OutputKbn from(String value) {
      return Arrays.stream(OutputKbn.values())
          .filter(outputKbn -> outputKbn.getValue().equals(value)).findFirst().orElse(null);
    }

  };

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

      Map<String, String> entryLog = gameEntryLog(gameEntryLogPath);

      OutputKbn outputKbn = args.length > 2 ? OutputKbn.from(args[2]) : OutputKbn.userRanking;

      switch (outputKbn) {
        case userRanking:
          Map<String, String[]> maxScoreLogPerUser = getMaxScoreLogPerUser(gameScoreLogPath);
          maxScoreLogPerUser = sortRankingScoreLogPerUser(maxScoreLogPerUser);
          Map<String, List<String>> rankingDataPerGame =
              getRankingData(maxScoreLogPerUser, entryLog);
          outputRankingData(rankingDataPerGame);
          break;
        case dateSummary:
          YearMonth targetYearMonth = args.length > 3 ? YearMonth.parse(args[3], uuuuMM) : null;
          Map<String, List<String[]>> scoreLogPerDate =
              getScoreLogPerDate(gameScoreLogPath, targetYearMonth);
          scoreLogPerDate = sortScoreLogPerDate(scoreLogPerDate);
          List<String> sumData = getSumDateData(scoreLogPerDate, entryLog);
          outputSumDateData(sumData);
          break;
      }

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
    if (args == null || args.length < 2) {
      throw new IllegalArgumentException("invalid args");
    }

    if (Files.notExists(Paths.get(args[0]))) {
      throw new IllegalArgumentException("not exists args File " + "args[0]:" + args[0]);
    }

    if (Files.notExists(Paths.get(args[1]))) {
      throw new IllegalArgumentException("not exists args File " + "args[1]:" + args[1]);
    }

    if (args.length > 2 && OutputKbn.from(args[2]) == null) {
      throw new IllegalArgumentException("not exists args outputKbn " + "args[2]:" + args[2]);
    }

    if (args.length > 3) {
      try {
        YearMonth.parse(args[3], uuuuMM);
      } catch (Exception e) {
        throw new IllegalArgumentException("not exists args outputKbn " + "args[3]:" + args[3], e);
      }
    }
  }

  /**
   * エントリーログを取得
   * 
   * @param gameEntryLogPath
   * @return エントリーログ
   * @throws IOException
   */
  private static Map<String, String> gameEntryLog(Path gameEntryLogPath) throws IOException {
    try (Stream<String> lines = Files.lines(gameEntryLogPath)) {
      return lines.skip(1).map(line -> line.split(","))
          .collect(toMap(values -> values[0], values -> values[1]));
    }
  }

  /**
   * 最高スコアログをユーザ単位に取得
   * 
   * @param gameScoreLogPath
   * @return 最高スコアログ
   * @throws IOException
   */
  private static Map<String, String[]> getMaxScoreLogPerUser(Path gameScoreLogPath)
      throws IOException {
    try (Stream<String> lines = Files.lines(gameScoreLogPath)) {
      return lines.skip(1).map(line -> line.split(","))
          .collect(Collectors.toMap(values -> values[1], Function.identity(),
              BinaryOperator.maxBy(Comparator.comparingInt(values -> Integer.valueOf(values[2])))));
    }
  }

  /**
   * スコアログを日付単位に取得
   * 
   * @param gameScoreLogPath
   * @return スコアログ
   * @throws IOException
   */
  private static Map<String, List<String[]>> getScoreLogPerDate(Path gameScoreLogPath,
      YearMonth targetYearMonth) throws IOException {
    try (Stream<String> lines = Files.lines(gameScoreLogPath)) {
      return lines
          .skip(
              1)
          .map(
              line -> line.split(","))
          .filter(values -> targetYearMonth != null ? YearMonth.parse(values[0],
              DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm")
                  .withResolverStyle(ResolverStyle.STRICT))
              .equals(targetYearMonth) : true)
          .collect(Collectors.groupingBy(values -> LocalDate
              .parse(values[0],
                  DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm")
                      .withResolverStyle(ResolverStyle.STRICT))
              .format(DateTimeFormatter.ofPattern("uuuu/MM/dd"))));
    }
  }

  /**
   * ユーザ単位のスコアログをランキング用にソート
   * 
   * @param scoreLogPerUser
   * @return ユーザ単位のスコアログ
   */
  private static Map<String, String[]> sortRankingScoreLogPerUser(
      Map<String, String[]> scoreLogPerUser) {

    Comparator<Map.Entry<String, String[]>> scoreComparator = Map.Entry.comparingByValue(
        Comparator.comparing(values -> Integer.valueOf(values[2]), Comparator.reverseOrder()));
    Comparator<Map.Entry<String, String[]>> gameKbnComparator =
        Map.Entry.comparingByValue(Comparator.comparing(values -> Integer.valueOf(values[3])));
    Comparator<Map.Entry<String, String[]>> playerIdComparator = Map.Entry.comparingByKey();

    return scoreLogPerUser.entrySet().stream()
        .sorted(gameKbnComparator.thenComparing(scoreComparator).thenComparing(playerIdComparator))
        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (oldVal, newVal) -> oldVal,
            LinkedHashMap::new));

  }

  /**
   * 日付単位のスコアログをソート
   * 
   * @param scoreLogPerUser
   * @return 日付単位のスコアログ
   */
  private static Map<String, List<String[]>> sortScoreLogPerDate(
      Map<String, List<String[]>> scoreLogPerDate) {

    return scoreLogPerDate.entrySet().stream().sorted(Map.Entry.comparingByKey()).collect(toMap(
        Map.Entry::getKey, Map.Entry::getValue, (oldVal, newVal) -> oldVal, LinkedHashMap::new));

  }

  /**
   * ランキングデータを取得
   * 
   * @param scoreLogPerUserSorted
   * @return ランキングデータ
   */
  private static Map<String, List<String>> getRankingData(
      Map<String, String[]> scoreLogPerUserSorted, Map<String, String> gameEntryLog) {
    int prevScore = 0;
    int rank = 0;
    int outRank = 0;
    Map<String, List<String>> rankingDataPerGame = new HashMap<>();
    List<String> rankingData = new ArrayList<>();
    for (Map.Entry<String, String[]> scoreLog : scoreLogPerUserSorted.entrySet()) {

      String gameKbn = scoreLog.getValue()[3];
      String playerId = scoreLog.getKey();
      String handleName = gameEntryLog.get(playerId);
      Integer score = Integer.valueOf(scoreLog.getValue()[2]);

      if (handleName == null) {
        continue;
      }

      if (!rankingDataPerGame.containsKey(gameKbn)) {
        rankingData = new ArrayList<>();
        rankingDataPerGame.put(gameKbn, rankingData);
        rank = 0;
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
    return rankingDataPerGame;

  }

  /**
   * 日付集計データを取得
   * 
   * @param scoreLogPerUserSorted
   * @return 集計データ
   */
  private static List<String> getSumDateData(Map<String, List<String[]>> scoreLogPerDateSorted,
      Map<String, String> gameEntryLog) {

    List<String> sumData = new ArrayList<>();
    for (Map.Entry<String, List<String[]>> scoreLog : scoreLogPerDateSorted.entrySet()) {

      String date = scoreLog.getKey();

      long unique_player_cnt = scoreLog.getValue().stream().map(values -> values[1])
          .filter(playerId -> gameEntryLog.get(playerId) != null).distinct().count();

      IntSummaryStatistics summary = scoreLog.getValue().stream().filter(values -> {
        String playerId = values[1];
        String handleName = gameEntryLog.get(playerId);
        return handleName != null;
      }).collect(Collectors.summarizingInt(values -> Integer.valueOf(values[2])));

      long playCnt = summary.getCount();
      int maxScore = summary.getMax();
      int minScore = summary.getMin();
      long avgScore = Math.round(summary.getAverage());

      sumData.add(date + "," + unique_player_cnt + "," + playCnt + "," + avgScore + "," + maxScore
          + "," + minScore);

    }
    return sumData;

  }

  /**
   * ランキングデータを出力
   * 
   * @param rankingData
   */
  private static void outputRankingData(Map<String, List<String>> rankingDataPerGame) {
    String lineFeedCode = "\n";
    StringBuilder sb = new StringBuilder();

    if (rankingDataPerGame.isEmpty()) {
      sb.append("no data" + lineFeedCode);
    }

    rankingDataPerGame.entrySet().stream().forEach(map -> {
      sb.append("game:" + map.getKey() + lineFeedCode);
      sb.append("rank,player_id,handle_name,score" + lineFeedCode);
      map.getValue().forEach(line -> sb.append(line + lineFeedCode));
    });
    System.out.print(sb.toString());
  }

  /**
   * 集計データを出力
   * 
   * @param sumDateData
   */
  private static void outputSumDateData(List<String> sumDateData) {
    String lineFeedCode = "\n";
    StringBuilder sb = new StringBuilder();
    sb.append("date,unique_player_cnt,play_cnt,score_avg,score_max,score_min" + lineFeedCode);
    sumDateData.forEach(line -> sb.append(line + lineFeedCode));
    System.out.print(sb.toString());
  }

}
