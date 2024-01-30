package ranking.v1_1;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class Main {

  public static void main(String[] args) throws IOException {

    ScoreLogs scoreLogs;
    try (Stream<String> lines = Files.lines(Paths.get(args[0]))) {
      scoreLogs = new ScoreLogs(lines);
    }

    PlayerScores playerScores = new PlayerScores(scoreLogs);

    Rankings rankings = new Rankings(playerScores);

    System.out.print(rankings.toCsvWithHeader());

  }
}
