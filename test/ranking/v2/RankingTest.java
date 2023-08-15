package ranking.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RankingTest {

  private ByteArrayOutputStream out;

  @BeforeEach
  public void init() {
    out = new ByteArrayOutputStream();
    System.setOut(new PrintStream(out));
  }

  @Test
  void mainTest_³íŒn() throws IOException {

    String[] args = {"testdata/game_ently_log.csv", "testdata/game_score_log.csv"};
    Ranking.main(args);

    Path path = Path.of("testdata/v2_exp_success.csv");
    String exp = Files.readString(path);
    assertEquals(exp, out.toString());
  }

  @Test
  void mainTest_³íŒn_‹óƒtƒ@ƒCƒ‹() throws IOException {

    String[] args = {"testdata/game_ently_log_none.csv", "testdata/game_score_log_none.csv"};
    Ranking.main(args);

    Path path = Path.of("testdata/v2_exp_none.csv");
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
