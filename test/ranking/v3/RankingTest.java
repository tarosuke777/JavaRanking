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
  void mainTest_正常系() throws IOException {

    String[] input = {"1", "testdata/ranking/v3/in/game_ently_log.csv",
        "testdata/ranking/v3/in/game_score_log.csv"};
    String output = Files.readString(Path.of("testdata/ranking/v3/out/success.csv"));

    Ranking.main(input);

    assertEquals(output, this.output.toString());
  }

  @Test
  void mainTest_正常系_空ファイル() throws IOException {

    String[] input = {"1", "testdata/ranking/v3/in/game_ently_log_none.csv",
        "testdata/ranking/v3/in/game_score_log_none.csv"};
    String output = Files.readString(Path.of("testdata/ranking/v3/out/none.csv"));

    Ranking.main(input);

    assertEquals(output, this.output.toString());
  }

  @Test
  void mainTest_正常系_日集計() throws IOException {

    String[] input = {"2", "testdata/ranking/v3/in/game_ently_log.csv",
        "testdata/ranking/v3/in/game_score_log.csv"};
    String output = Files.readString(Path.of("testdata/ranking/v3/out/success_sum.csv"));

    Ranking.main(input);

    assertEquals(output, this.output.toString());
  }

  @Test
  void mainTest_正常系_日集計_年月指定() throws IOException {

    String[] input = {"2", "testdata/ranking/v3/in/game_ently_log.csv",
        "testdata/ranking/v3/in/game_score_log.csv", "202101"};
    String output = Files.readString(Path.of("testdata/ranking/v3/out/success_sum_202101.csv"));

    Ranking.main(input);

    assertEquals(output, this.output.toString());
  }

  // @Test
  void mainTest_異常系_日集計_不正年月指定() throws IOException {

    String[] input = {"2", "testdata/ranking/v3/in/game_ently_log.csv",
        "testdata/ranking/v3/in/game_score_log.csv", "202100"};

    Ranking.main(input);
  }

  @Test
  void mainTest_正常系_上位平均ランキング() throws IOException {

    String[] input = {"3", "testdata/ranking/v3/in/game_ently_log.csv",
        "testdata/ranking/v3/in/game_score_log.csv"};
    String output = Files.readString(Path.of("testdata/ranking/v3/out/success_avg_ranking.csv"));

    Ranking.main(input);

    assertEquals(output, this.output.toString());
  }

  @Test
  void mainTest_正常系_ゲーム種別毎のランキング() throws IOException {

    String[] input = {"4", "testdata/ranking/v3/in/game_ently_log.csv",
        "testdata/ranking/v3/in/game_score_log_small.csv", "testdata/ranking/v3/in/game_kbn.csv",};
    String output =
        Files.readString(Path.of("testdata/ranking/v3/out/success_user_game_per_ranking.csv"));

    Ranking.main(input);

    assertEquals(output, this.output.toString());
  }
}
