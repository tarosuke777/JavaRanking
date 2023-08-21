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
  void mainTest_����n() throws IOException {

    String[] input =
        {"testdata/ranking/v3/in/game_ently_log.csv", "testdata/ranking/v3/in/game_score_log.csv"};
    String output = Files.readString(Path.of("testdata/ranking/v3/out/success.csv"));

    Ranking.main(input);

    assertEquals(output, this.output.toString());
  }

  @Test
  void mainTest_����n_��t�@�C��() throws IOException {

    String[] input = {"testdata/ranking/v3/in/game_ently_log_none.csv",
        "testdata/ranking/v3/in/game_score_log_none.csv"};
    String output = Files.readString(Path.of("testdata/ranking/v3/out/none.csv"));

    Ranking.main(input);

    assertEquals(output, this.output.toString());
  }

  @Test
  void mainTest_����n_���W�v() throws IOException {

    String[] input = {"testdata/ranking/v3/in/game_ently_log.csv",
        "testdata/ranking/v3/in/game_score_log.csv", "2"};
    String output = Files.readString(Path.of("testdata/ranking/v3/out/success_sum.csv"));

    Ranking.main(input);

    assertEquals(output, this.output.toString());
  }

  @Test
  void mainTest_����n_���W�v_�N���w��() throws IOException {

    String[] input = {"testdata/ranking/v3/in/game_ently_log.csv",
        "testdata/ranking/v3/in/game_score_log.csv", "2", "202101"};
    String output = Files.readString(Path.of("testdata/ranking/v3/out/success_sum_202101.csv"));

    Ranking.main(input);

    assertEquals(output, this.output.toString());
  }

  // @Test
  void mainTest_�ُ�n_���W�v_�s���N���w��() throws IOException {

    String[] input = {"testdata/ranking/v3/in/game_ently_log.csv",
        "testdata/ranking/v3/in/game_score_log.csv", "2", "202100"};

    Ranking.main(input);
  }

  @Test
  void mainTest_����n_��ʕ��σ����L���O() throws IOException {

    String[] input = {"testdata/ranking/v3/in/game_ently_log.csv",
        "testdata/ranking/v3/in/game_score_log.csv", "3"};
    String output = Files.readString(Path.of("testdata/ranking/v3/out/success_avg_ranking.csv"));

    Ranking.main(input);

    assertEquals(output, this.output.toString());
  }

  @Test
  void mainTest_����n_�Q�[����ʖ��̃����L���O() throws IOException {

    String[] input = {"testdata/ranking/v3/in/game_ently_log.csv",
        "testdata/ranking/v3/in/game_score_log_small.csv", "4"};
    String output =
        Files.readString(Path.of("testdata/ranking/v3/out/success_user_game_per_ranking.csv"));

    Ranking.main(input);

    assertEquals(output, this.output.toString());
  }
}
