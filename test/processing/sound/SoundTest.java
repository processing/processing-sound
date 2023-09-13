package processing.sound;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class SoundTest {

	private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

	@Test
	public void testList() {
		// System.setOut(new PrintStream(outputStreamCaptor));
		// Sound.list();
		// assertEquals("asd", outputStreamCaptor.toString());
	}
}

