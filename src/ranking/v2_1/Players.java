package ranking.v2_1;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Players {

  record Player(String playerId, String handleName) {
  }

  private List<Player> players;

  public Players(Path gameEntryLogPath) throws IOException {

    try (Stream<String> lines = Files.lines(gameEntryLogPath)) {
      this.players = lines.skip(1).map(line -> line.split(","))
          .map(values -> new Player(values[0], values[1])).collect(Collectors.toList());
    }
  }

  public List<Player> players() {
    return this.players;
  }

  public Player player(String playerId) {

    return this.players.stream().filter(player -> player.playerId.equals(playerId)).findFirst()
        .get();

  }

}
