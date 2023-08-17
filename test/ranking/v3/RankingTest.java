package ranking.v3;

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

    String[] input =
        {"testdata/ranking/v3/in/game_ently_log.csv", "testdata/ranking/v3/in/game_score_log.csv"};
    String output = Files.readString(Path.of("testdata/ranking/v3/out/success.csv"));

    Ranking.main(input);

    assertEquals(output, this.output.toString());
  }

  @Test
  void mainTest_³íŒn_‹óƒtƒ@ƒCƒ‹() throws IOException {

    String[] input = {"testdata/ranking/v3/in/game_ently_log_none.csv",
        "testdata/ranking/v3/in/game_score_log_none.csv"};
    String output = Files.readString(Path.of("testdata/ranking/v3/out/none.csv"));

    Ranking.main(input);

    assertEquals(output, this.output.toString());
  }

  @Test
  void mainTest_³íŒn_“úWŒv() throws IOException {

    String[] input = {"testdata/ranking/v3/in/game_ently_log.csv",
        "testdata/ranking/v3/in/game_score_log.csv", "2"};
    String output = Files.readString(Path.of("testdata/ranking/v3/out/success_sum.csv"));

    Ranking.main(input);

    assertEquals(output, this.output.toString());
  }
}
