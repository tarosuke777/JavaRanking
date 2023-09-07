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
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Ranking {

  private static DateTimeFormatter uuuuMM =
      DateTimeFormatter.ofPattern("uuuuMM").withResolverStyle(ResolverStyle.STRICT);

  private enum OutputKbn {
    userRanking("1"), dateSummary("2"), userAvgRanking("3"), userGamePerRanking("4");

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

      OutputKbn outputKbn = OutputKbn.from(args[0]);

      Path gameEntryLogPath = Paths.get(args[1]);
      Path gameScoreLogPath = Paths.get(args[2]);

      Map<String, String> entryLog = gameEntryLog(gameEntryLogPath);

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
        case userAvgRanking:
          maxScoreLogPerUser = getMaxScoreLogPerUser(gameScoreLogPath);
          maxScoreLogPerUser = sortRankingScoreLogPerUser(maxScoreLogPerUser);
          rankingDataPerGame = getAvgRankingData(maxScoreLogPerUser, entryLog);
          outputRankingData(rankingDataPerGame);
          break;
        case userGamePerRanking:
          Path gameKbnPath = Paths.get(args[3]);
          Map<String, String> gameKbnToName = getGameKbnToName(gameKbnPath);

          Map<String, Map<String, Optional<String[]>>> gameKbnToPlayerIdToScoreLog =
              getGameKbnToPlayerIdToMaxScoreLog(gameScoreLogPath);

          Map<String, Map<String, Optional<String[]>>> gameKbnToPlayerIdToScoreLogSorted =
              sortScoreLog(gameKbnToPlayerIdToScoreLog);

          Map<String, Map<String, String>> gameKbnToPlayerIdToRank = getGameKbnToPlayerIdToRank(
              gameKbnToPlayerIdToScoreLogSorted, entryLog, gameKbnToName);

          List<String> rankingData =
              getPlayerIdToRankingData(gameKbnToPlayerIdToRank, entryLog, gameKbnToName);
          outputPlayerIdGamePerRanking(rankingData);
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

    if (args == null || args.length < 3) {
      throw new IllegalArgumentException("invalid args");
    }

    if (OutputKbn.from(args[0]) == null) {
      throw new IllegalArgumentException("not exists args outputKbn " + "args[0]:" + args[0]);
    }

    if (Files.notExists(Paths.get(args[1]))) {
      throw new IllegalArgumentException(
          "not exists args gameEntryLogPath " + "args[1]:" + args[1]);
    }

    if (Files.notExists(Paths.get(args[2]))) {
      throw new IllegalArgumentException(
          "not exists args gameScoreLogPath " + "args[2]:" + args[2]);
    }


    switch (OutputKbn.from(args[0])) {
      case dateSummary:
        try {
          if (args.length > 3) {
            YearMonth.parse(args[3], uuuuMM);
          }
        } catch (Exception e) {
          throw new IllegalArgumentException("not exists args YearMonth " + "args[3]:" + args[3],
              e);
        }
        break;
      case userGamePerRanking:
        if (args.length < 4) {
          throw new IllegalArgumentException("invalid args gameKbnPath");
        }

        if (Files.notExists(Paths.get(args[3]))) {
          throw new IllegalArgumentException("not exists args gameKbnPath " + "args[3]:" + args[3]);
        }
      default:
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
          .sorted(Comparator.comparing(values -> values[0])).collect(toMap(values -> values[0],
              values -> values[1], (oldVal, newVal) -> oldVal, LinkedHashMap::new));
    }
  }

  /**
   * エントリーログを取得
   * 
   * @param gameEntryLogPath
   * @return エントリーログ
   * @throws IOException
   */
  private static Map<String, String> getGameKbnToName(Path gameKbnPath) throws IOException {
    try (Stream<String> lines = Files.lines(gameKbnPath)) {
      return lines.skip(1).map(line -> line.split(","))
          .sorted(Comparator.comparing(values -> values[0])).collect(toMap(values -> values[0],
              values -> values[1], (oldVal, newVal) -> oldVal, LinkedHashMap::new));
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
   * 最高スコアログをゲームかつユーザ単位に取得
   * 
   * @param gameScoreLogPath
   * @return 最高スコアログ
   * @throws IOException
   */
  private static Map<String, Map<String, Optional<String[]>>> getGameKbnToPlayerIdToMaxScoreLog(
      Path gameScoreLogPath) throws IOException {
    try (Stream<String> lines = Files.lines(gameScoreLogPath)) {
      return lines.skip(1).map(line -> line.split(","))
          .collect(Collectors.groupingBy(values -> values[3], Collectors.groupingBy(
              values -> values[1],
              Collectors.maxBy(Comparator.comparingInt(values -> Integer.valueOf(values[2]))))));
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
   * ユーザ単位のスコアログをランキング用にソート
   * 
   * @param scoreLogPerUser
   * @return ユーザ単位のスコアログ
   */
  private static Map<String, Map<String, Optional<String[]>>> sortScoreLog(
      Map<String, Map<String, Optional<String[]>>> scoreLogPerGamePerUser) {

    Comparator<Map.Entry<String, Optional<String[]>>> playerIdComparator =
        Map.Entry.comparingByKey();

    Comparator<Map.Entry<String, Optional<String[]>>> scoreComparator =
        Map.Entry.comparingByValue(Comparator.comparing(values -> Integer.valueOf(values.get()[2]),
            Comparator.reverseOrder()));

    scoreLogPerGamePerUser.entrySet().stream().forEach(game -> {
      game.getValue().entrySet().stream().sorted(scoreComparator.thenComparing(playerIdComparator))
          .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (oldVal, newVal) -> oldVal,
              LinkedHashMap::new));
    });

    return scoreLogPerGamePerUser;
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
   * 平均ランキングデータを取得
   * 
   * @param scoreLogPerUserSorted
   * @return 平均ランキングデータ
   */
  private static Map<String, List<String>> getAvgRankingData(
      Map<String, String[]> scoreLogPerUserSorted, Map<String, String> gameEntryLog) {

    double avgScore = 0.0;
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
        avgScore = scoreLogPerUserSorted.entrySet().stream()
            .filter(mapEntry -> gameKbn.equals(mapEntry.getValue()[3]))
            .collect(Collectors.averagingInt(mapEntry -> Integer.valueOf(mapEntry.getValue()[2])));
      }

      if (score < avgScore) {
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
    return rankingDataPerGame;

  }

  /**
   * ユーザかつゲーム毎のランキングデータを取得
   * 
   * @param scoreLogPerUserSorted
   * @return 平均ランキングデータ
   */
  private static List<String> getPlayerIdToRankingData(
      Map<String, Map<String, String>> gameKbnToPlayerIdToRank, Map<String, String> gameEntryLog,
      Map<String, String> gameKbnToName) {

    return gameEntryLog.entrySet().stream().map(gameEntryLog_ -> {
      String playerId = gameEntryLog_.getKey();
      String handleName = gameEntryLog_.getValue();

      List<String> gameScoreRankings = gameKbnToName.keySet().stream().map(gameKbn -> {
        Map<String, String> userToRank = gameKbnToPlayerIdToRank.get(gameKbn);
        return userToRank.containsKey(playerId) ? userToRank.get(playerId) : "";
      }).collect(Collectors.toList());

      return playerId + "," + handleName + "," + String.join(",", gameScoreRankings);

    }).collect(Collectors.toList());
  }

  private static Map<String, Map<String, String>> getGameKbnToPlayerIdToRank(
      Map<String, Map<String, Optional<String[]>>> gameKbnToPlayerIdToScoreLog,
      Map<String, String> gameEntryLog, Map<String, String> gameKbnToName) {

    Map<String, Map<String, String>> gameKbnToPlayerIdToRank = new HashMap<>();

    gameKbnToName.keySet().stream().forEach(gameKbn -> {
      Map<String, Optional<String[]>> playerIdToScoreLog = gameKbnToPlayerIdToScoreLog.get(gameKbn);
      Map<String, String> playerIdToRank = getPlayerIdToRank(playerIdToScoreLog, gameEntryLog);
      gameKbnToPlayerIdToRank.put(gameKbn, playerIdToRank);
    });

    return gameKbnToPlayerIdToRank;
  }


  private static Map<String, String> getPlayerIdToRank(
      Map<String, Optional<String[]>> playerIdToScoreLog, Map<String, String> gameEntryLog) {

    Map<String, String> playerIdToRankingData = new HashMap<>();

    int prevScore = 0;
    int rank = 0;
    int outRank = 0;

    for (Map.Entry<String, Optional<String[]>> _playerIdToScoreLog : playerIdToScoreLog
        .entrySet()) {

      String playerId = _playerIdToScoreLog.getKey();
      String handleName = gameEntryLog.get(playerId);
      Integer score = Integer.valueOf(_playerIdToScoreLog.getValue().get()[2]);

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

      playerIdToRankingData.put(playerId, String.valueOf(outRank));
      prevScore = score;
    }

    return playerIdToRankingData;
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
   * ランキングデータを出力
   * 
   * @param rankingData
   */
  private static void outputPlayerIdGamePerRanking(List<String> rankingData) {
    String lineFeedCode = "\n";
    StringBuilder sb = new StringBuilder();

    if (rankingData.isEmpty()) {
      sb.append("no data" + lineFeedCode);
    } else {
      sb.append("player_id,handle_name,game_kbn_1,game_kbn_2" + lineFeedCode);
    }
    rankingData.stream().forEach(out -> {
      sb.append(out + lineFeedCode);
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
