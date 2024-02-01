package ranking.v2_1;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Scores {

  record Score(LocalDate createTimestamp, String playerId, int score) {
  }

  private List<Score> scores;

  public Scores(Path gameScoreLogPath) throws IOException {
    try (Stream<String> lines = Files.lines(gameScoreLogPath)) {
      this.scores = lines.skip(1).map(line -> line.split(","))
          .map(values -> new Score(
              LocalDate.parse(values[0], DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
              values[1], Integer.parseInt(values[2])))
          .collect(Collectors.toList());
    }

  }

  public List<Score> scores() {
    return this.scores;
  }

}
