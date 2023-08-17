package ranking.v3;


import static java.util.stream.Collectors.toMap;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.IntSummaryStatistics;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Ranking {

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

      OutputKbn outputKbn =
          args.length > 2 && args[2] != null ? OutputKbn.from(args[2]) : OutputKbn.userRanking;

      switch (outputKbn) {
        case userRanking:
          Map<String, String[]> scoreLogPerUser = getScoreLogPerUser(gameScoreLogPath);
          Map<String, String[]> scoreLogPerUserSorted = sortScoreLogPerUser(scoreLogPerUser);
          List<String> rankingData = getRankingData(scoreLogPerUserSorted, entryLog);
          outputRankingData(rankingData);
          break;
        case dateSummary:
          Map<String, List<String[]>> scoreLogPerDate = getScoreLogPerDate(gameScoreLogPath);
          Map<String, List<String[]>> scoreLogPerDateSorted = sortScoreLogPerDate(scoreLogPerDate);
          List<String> sumData = getSumDateData(scoreLogPerDateSorted, entryLog);
          outputSumDateData(sumData);

          break;
      }

    } catch (

    Exception e) {
      e.printStackTrace();
      System.exit(1);
    }

  }

  /**
   * ���s�������̑Ó������`�F�b�N
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
  }

  /**
   * �G���g���[���O���擾
   * 
   * @param gameEntryLogPath
   * @return �G���g���[���O
   * @throws IOException
   */
  private static Map<String, String> gameEntryLog(Path gameEntryLogPath) throws IOException {
    try (Stream<String> lines = Files.lines(gameEntryLogPath)) {
      return lines.skip(1).map(line -> line.split(","))
          .collect(toMap(values -> values[0], values -> values[1]));
    }
  }

  /**
   * �X�R�A���O�����[�U�P�ʂɎ擾
   * 
   * @param gameScoreLogPath
   * @return �X�R�A���O
   * @throws IOException
   */
  private static Map<String, String[]> getScoreLogPerUser(Path gameScoreLogPath)
      throws IOException {
    try (Stream<String> lines = Files.lines(gameScoreLogPath)) {
      return lines.skip(1).map(line -> line.split(","))
          .collect(Collectors.toMap(values -> values[1], Function.identity(),
              BinaryOperator.maxBy(Comparator.comparingInt(values -> Integer.valueOf(values[2])))));
    }
  }

  /**
   * �X�R�A���O����t�P�ʂɎ擾
   * 
   * @param gameScoreLogPath
   * @return �X�R�A���O
   * @throws IOException
   */
  private static Map<String, List<String[]>> getScoreLogPerDate(Path gameScoreLogPath)
      throws IOException {
    try (Stream<String> lines = Files.lines(gameScoreLogPath)) {
      return lines.skip(1).map(line -> line.split(","))
          .collect(Collectors.groupingBy(values -> LocalDate
              .parse(values[0],
                  DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm")
                      .withResolverStyle(ResolverStyle.STRICT))
              .format(DateTimeFormatter.ofPattern("uuuu/MM/dd"))));
    }
  }

  /**
   * ���[�U�P�ʂ̃X�R�A���O���\�[�g
   * 
   * @param scoreLogPerUser
   * @return �v���C���[���O�f�[�^
   */
  private static Map<String, String[]> sortScoreLogPerUser(Map<String, String[]> scoreLogPerUser) {

    Comparator<Map.Entry<String, String[]>> valueComparator = Map.Entry.comparingByValue(
        Comparator.comparing(values -> Integer.valueOf(values[2]), Comparator.reverseOrder()));
    Comparator<Map.Entry<String, String[]>> keyComparator = Map.Entry.comparingByKey();

    return scoreLogPerUser.entrySet().stream().sorted(valueComparator.thenComparing(keyComparator))
        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (oldVal, newVal) -> oldVal,
            LinkedHashMap::new));

  }

  /**
   * ���t�P�ʂ̃X�R�A���O���\�[�g
   * 
   * @param scoreLogPerUser
   * @return �v���C���[���O�f�[�^
   */
  private static Map<String, List<String[]>> sortScoreLogPerDate(
      Map<String, List<String[]>> scoreLogPerDate) {

    return scoreLogPerDate.entrySet().stream().sorted(Map.Entry.comparingByKey()).collect(toMap(
        Map.Entry::getKey, Map.Entry::getValue, (oldVal, newVal) -> oldVal, LinkedHashMap::new));

  }

  /**
   * �����L���O�f�[�^���擾
   * 
   * @param scoreLogPerUserSorted
   * @return �����L���O�f�[�^
   */
  private static List<String> getRankingData(Map<String, String[]> scoreLogPerUserSorted,
      Map<String, String> gameEntryLog) {
    int prevScore = 0;
    int rank = 0;
    int outRank = 0;
    List<String> rankingData = new ArrayList<>();
    for (Map.Entry<String, String[]> scoreLog : scoreLogPerUserSorted.entrySet()) {

      String playerId = scoreLog.getKey();
      String handleName = gameEntryLog.get(playerId);
      Integer score = Integer.valueOf(scoreLog.getValue()[2]);

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
   * ���t�W�v�f�[�^���擾
   * 
   * @param scoreLogPerUserSorted
   * @return �W�v�f�[�^
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
   * �����L���O�f�[�^���o��
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

  /**
   * �W�v�f�[�^���o��
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