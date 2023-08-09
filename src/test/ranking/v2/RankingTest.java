package test.ranking.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import ranking.v2.Ranking;

class RankingTest {

  @Test
  void mainTest_³íŒn() throws IOException {

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    System.setOut(new PrintStream(out));

    String[] args =
        {"./src/test/ranking/v2/game_ently_log.csv", "./src/test/ranking/v2/game_score_log.csv"};
    Ranking.main(args);

    Path path = Path.of("./src/test/ranking/v2/expMainTest.csv");
    String exp = Files.readString(path);
    assertEquals(exp, out.toString());
  }

  @Test
  void mainTest_³íŒn_‹óƒtƒ@ƒCƒ‹() throws IOException {

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    System.setOut(new PrintStream(out));

    String[] args = {"./src/test/ranking/v2/game_ently_log_none.csv",
        "./src/test/ranking/v2/game_score_log_none.csv"};
    Ranking.main(args);

    Path path = Path.of("./src/test/ranking/v2/exp_none.csv");
    String exp = Files.readString(path);
    assertEquals(exp, out.toString());
  }

  // @Test
  // void mainTest_ˆÙíŒn_ˆø”‚Ìƒtƒ@ƒCƒ‹‚ª‘¶Ý‚µ‚È‚¢() throws IOException {
  // ByteArrayOutputStream out = new ByteArrayOutputStream();
  // System.setErr(new PrintStream(out));
  //
  // String[] args = {"./src/test/ranking/v2/nothing_game_ently_log.csv",
  // "./src/test/ranking/v2/nothing_game_score_log.csv"};
  // Ranking.main(args);
  //
  // String exp = "not exists args File";
  // assertEquals(exp, out.toString());
  //
  // }

  // @Test
  // void mainTest_ˆÙíŒn_ˆø”NULL() throws IOException {
  // NullPointerException e = assertThrows(NullPointerException.class, () -> {
  // Ranking.main(null);
  // });
  // assertEquals("Cannot load from object array because \"args\" is null", e.getMessage());
  // }

}
