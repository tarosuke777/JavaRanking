package ranking.v1;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingInt;
import static java.util.stream.Collectors.toMap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

public class Ranking {

	public static void main(String[] args) throws IOException {
		
		var playerLogFilePath = Paths.get(args[0]);
				
		Map<String, Integer> playerLogData = getPlayLogData(playerLogFilePath);
		
		playerLogData = sortPlayLogData(playerLogData);
		
		String rankingData = getRankingData(playerLogData);
		
		outputRankingData(rankingData);
	}
	
	private static void outputRankingData(String rankingData) throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append("rank,id,totalScore" + System.lineSeparator());
		sb.append(rankingData);
		System.out.println(sb.toString());
	}

	private static String getRankingData(Map<String, Integer> sortedPlayerIdWithSumScore) {
		int rank = 0;
		int rankScore = 0;
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, Integer> sorted : sortedPlayerIdWithSumScore.entrySet()) {
			if (isRankDown(rankScore, sorted)) {
				rank += 1;
				rankScore = sorted.getValue();
			}
			if (rank > 10) {
				break;
			}

			sb.append(rank + "," + sorted.getKey() + "," + sorted.getValue() + System.lineSeparator());
		}
		return sb.toString();
	}

	private static Map<String, Integer> sortPlayLogData(Map<String, Integer> playerIdWithSumScore) {
		return playerIdWithSumScore.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	}

	private static Map<String, Integer> getPlayLogData(Path playerLogFilePath) throws IOException {
		try (Stream<String> data = Files.lines(playerLogFilePath)) {
			return data.skip(1) // header
					.map(line -> line.split(","))
					.collect(groupingBy(values -> values[1], summingInt(values -> Integer.parseInt(values[2]))));

		} 
	}

	private static boolean isRankDown(int rankScore, Map.Entry<String, Integer> sorted) {
		return rankScore == 0 || rankScore > sorted.getValue();
	}

}
