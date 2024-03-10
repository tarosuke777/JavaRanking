package ranking.v2_2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class App {

  public static void main(String[] args) {

    try {
      validateArgs(args);
    } catch (IllegalArgumentException e) {
      System.err.println(e.getMessage());
      System.exit(1);
    }

    Map<String, String> playerIdWithName = new HashMap<>();
    try {
      playerIdWithName = getPlayerIdWithName(args[0]);
    } catch (IOException e) {
      e.printStackTrace();
      System.err.println("read error entry log");
      System.exit(1);
    }

    Map<String, Integer> playerIdWithMaxScore = new HashMap<>();
    try {
      playerIdWithMaxScore = getPlayerIdWithMaxScore(args[1]);
    } catch (IOException e) {
      e.printStackTrace();
      System.err.println("read error score log");
      System.exit(1);
    }

    Map<String, Integer> playerIdWithMaxScoreSorted =
        getSortedPlayerIdWithMaxScore(playerIdWithMaxScore);

    List<String> rankingData = getRankingData(playerIdWithName, playerIdWithMaxScoreSorted);

    outRankingData(rankingData);

  }

  /**
   * �����L���O�f�[�^�̏o��
   * 
   * @param rankingData
   */
  private static void outRankingData(List<String> rankingData) {
    System.out.print("rank,player_id,handle_name,score" + "\n");
    rankingData.stream().forEach(data -> System.out.print(data + "\n"));
  }

  /**
   * �����L���O�f�[�^�̍쐬�A�ԋp
   * 
   * @param playerIdWithName
   * @param playerIdWithMaxScoreSorted
   * @return �����L���O�f�[�^
   */
  private static List<String> getRankingData(Map<String, String> playerIdWithName,
      Map<String, Integer> playerIdWithMaxScoreSorted) {
    int rank = 0;
    int dispRank = 0;
    int prevScore = 0;

    List<String> rankingData = new ArrayList<>();

    for (Map.Entry<String, Integer> entry : playerIdWithMaxScoreSorted.entrySet()) {
      String playerId = entry.getKey();
      Integer score = entry.getValue();
      String handleName = playerIdWithName.get(playerId);

      if (handleName == null) {
        continue;
      }

      rank++;
      if (prevScore != score) {
        dispRank = rank;
      }

      if (dispRank > 10) {
        break;
      }

      rankingData.add(dispRank + "," + playerId + "," + handleName + "," + score);

      prevScore = score;

    }
    return rankingData;
  }

  /**
   * �v���C���[ID�ƃX�R�A�̃}�b�s���O�����\�[�g
   * 
   * @param playerIdWithMaxScore
   * @return �\�[�g�����v���C���[ID�ƃX�R�A�̃}�b�s���O���
   */
  private static Map<String, Integer> getSortedPlayerIdWithMaxScore(
      Map<String, Integer> playerIdWithMaxScore) {
    return playerIdWithMaxScore.entrySet().stream()
        .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())
            .thenComparing(Map.Entry.comparingByKey()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
            LinkedHashMap::new));

  }

  /**
   * �v���C���[ID�ƍő�X�R�A�̃}�b�s���O�����쐬�A�ԋp
   * 
   * @param args1 �X�R�A���O�t�@�C���p�X
   * @return �v���C���[ID�ƍő�X�R�A�̃}�b�s���O���
   * @throws IOException �X�R�A���O�t�@�C���̓ǂݍ��݃G���[
   */
  private static Map<String, Integer> getPlayerIdWithMaxScore(String args1) throws IOException {

    try (Stream<String> lines = Files.lines(Paths.get(args1));) {
      Map<String, Optional<String[]>> playerIdWithMaxLine = new HashMap<>();
      playerIdWithMaxLine = lines.skip(1).map(line -> line.split(","))
          .collect(Collectors.groupingBy(values -> values[1],
              Collectors.maxBy(Comparator.comparing(values -> Integer.valueOf(values[2])))));

      return playerIdWithMaxLine.entrySet().stream().collect(
          Collectors.toMap(Map.Entry::getKey, entry -> Integer.valueOf(entry.getValue().get()[2])));

    }

  }

  /**
   * �v���C���[ID�Ɩ��O�̃}�b�s���O�����쐬�A�ԋp
   * 
   * @param args0 �G���g���[���O�t�@�C���p�X
   * @return �v���C���[ID�Ɩ��O�̃}�b�s���O���
   * @throws IOException �G���g���[���O�t�@�C���ǂݍ��݃G���[
   */
  private static Map<String, String> getPlayerIdWithName(String args0) throws IOException {
    try (Stream<String> lines = Files.lines(Paths.get(args0));) {
      return lines.skip(1).map(line -> line.split(","))
          .collect(Collectors.toMap(values -> values[0], values -> values[1]));
    }
  }

  /**
   * �����̃o���f�[�V����
   * 
   * @param args
   * @throws IllegalArgumentException �o���f�[�V�����G���[
   */
  private static void validateArgs(String[] args) throws IllegalArgumentException {
    if (args == null || args.length != 2) {
      throw new IllegalArgumentException("invalid args.");
    }
    if (Files.notExists(Paths.get(args[0]))) {
      throw new IllegalArgumentException("entry file not exists.");
    }
    if (Files.notExists(Paths.get(args[1]))) {
      throw new IllegalArgumentException("score file not exists.");
    }
  }

}
