package ca.polymtl.inf4402.tp3;

import java.io.BufferedReader;
import java.io.IOException;

public class Utils {
	public static int getIntInput(BufferedReader input) throws IOException {
		while (true) {
			String line = input.readLine();
			try {
				int val = Integer.parseInt(line);
				return val;
			} catch (NumberFormatException e) {
				// ignore
			}
		}
	}

}
