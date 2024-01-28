package ranking.v1_1;

import java.io.IOException;
import java.nio.file.Paths;

public class RankingOutputAction {

  public static void main(String[] args) throws IOException {

    ScoreLogs scoreLogs = new ScoreLogs(Paths.get(args[0]));

    PlayerScores playerScores = new PlayerScores(scoreLogs);

    Rankings rankings = new Rankings(playerScores);

    rankings.toCsvForConsole();

  }
}
