package ranking.v2_1;


import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {

  public static void main(String[] args) {

    try {
      validateArgs(args);
    } catch (IllegalArgumentException e) {
      System.err.println(e.getMessage());
      System.exit(1);
    }

    try {

      Players players = new Players(Paths.get(args[0]));
      Scores scores = new Scores(Paths.get(args[1]));
      PlayerScores playerScores = new PlayerScores(scores);
      Rankings rankings = new Rankings(players, playerScores);
      System.out.println(rankings.toCsvWithHeader());


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

}
