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
		StringBuilder sb = new StringBuilder();
		sb.append("rank,id,totalScore" + System.lineSeparator());
		sb.append(addRankToString(sort(sum(Paths.get(args[0])))));
		System.out.println(sb.toString());
	}

	protected static String addRankToString(Map<String, Integer> sortedPlayerIdWithSumScore) {
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

	protected static Map<String, Integer> sort(Map<String, Integer> playerIdWithSumScore) {
		return playerIdWithSumScore.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	}

	protected static Map<String, Integer> sum(Path path) throws IOException {
		try (Stream<String> stream = Files.lines(path)) {
			return stream.skip(1) // header
					.map(line -> line.split(","))
					.collect(groupingBy(str -> str[1], summingInt(str -> Integer.parseInt(str[2]))));

		}
	}

	protected static boolean isRankDown(int rankScore, Map.Entry<String, Integer> sorted) {
		return rankScore == 0 || rankScore > sorted.getValue();
	}

}
