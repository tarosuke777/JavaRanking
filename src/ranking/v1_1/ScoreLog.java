package ranking.v1_1;

import java.time.LocalDate;

public record ScoreLog(LocalDate date, String playerId, int score) {
}
