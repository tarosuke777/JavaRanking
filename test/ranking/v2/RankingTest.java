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

  private ByteArrayOutputStream output;

  @BeforeEach
  public void init() {
    output = new ByteArrayOutputStream();
    System.setOut(new PrintStream(output));
  }

  @Test
  void mainTest_³íŒn() throws IOException {

    String[] input = {"testdata/game_ently_log.csv", "testdata/game_score_log.csv"};
    String output = Files.readString(Path.of("testdata/v2_exp_success.csv"));

    Ranking.main(input);

    assertEquals(output, this.output.toString());
  }

  @Test
  void mainTest_³íŒn_‹óƒtƒ@ƒCƒ‹() throws IOException {

    String[] input = {"testdata/game_ently_log_none.csv", "testdata/game_score_log_none.csv"};
    String output = Files.readString(Path.of("testdata/v2_exp_none.csv"));

    Ranking.main(input);

    assertEquals(output, this.output.toString());
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
