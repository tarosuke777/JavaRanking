package ranking.v1_1;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ScoreLogs {

  private List<ScoreLog> scoreLogs;

  public ScoreLogs(Stream<String> lines) {
    this.scoreLogs = lines.skip(1) // header
        .map(line -> line.split(","))
        .map(values -> new ScoreLog(
            LocalDate.parse(values[0], DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")), values[1],
            Integer.parseInt(values[2])))
        .collect(Collectors.toList());
  }

  public List<ScoreLog> getScoreLogs() {
    return this.scoreLogs;
  }
}
