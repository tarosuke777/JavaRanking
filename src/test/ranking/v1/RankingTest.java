package test.ranking.v1;

import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import ranking.v1.Ranking;

class RankingTest {

  @Test
  void mainTest_³íŒn() throws IOException {

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    System.setOut(new PrintStream(out));

    String[] args = {"./src/test/ranking/v1/score_log.csv"};
    Ranking.main(args);

    Path path = Path.of("./src/test/ranking/v1/expMainTest.csv");
    String exp = Files.readString(path);
    assertEquals(exp, out.toString());
  }

  @Test
  void mainTest_ˆÙíŒn_ˆø”NULL() throws IOException {
    NullPointerException e = assertThrows(NullPointerException.class, () -> {
      Ranking.main(null);
    });
    assertEquals("Cannot load from object array because \"args\" is null", e.getMessage());
  }

}
