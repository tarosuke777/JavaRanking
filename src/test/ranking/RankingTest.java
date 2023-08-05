package test.ranking;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import ranking.Ranking;

class RankingTest {

	@Test
	void mainTest() throws IOException {
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		System.setOut(new PrintStream(out));
		
		String[] args = {"./src/test/ranking/score_log.csv"};
		Ranking.main(args);
		
		Path path = Path.of("./src/test/ranking/expMainTest.csv");
		String exp = Files.readString(path);
		assertEquals(exp, out.toString());
	}
}
