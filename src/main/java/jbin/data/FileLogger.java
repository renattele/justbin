package jbin.data;

import jbin.domain.Logger;

import java.io.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class FileLogger implements Logger {
	private PrintWriter writer = null;

	public FileLogger(String file, String errorFile) {
		try {
			System.setErr(System.out);
			//writer = new PrintWriter(new FileOutputStream(file, true));
			writer = new PrintWriter(System.out);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void log(String type, String tag, String message) {
		var date = DateTimeFormatter.ISO_INSTANT.format(Instant.now());
		writer.println(date + " " + type + ": " + tag + ": " + message);
		writer.flush();
	}

	@Override
	public void info(String tag, String message) {
		log("INFO", tag, message);
	}

	@Override
	public void debug(String tag, String message) {
		log("DEBUG", tag, message);

	}

	@Override
	public void warn(String tag, String message) {
		log("WARN", tag, message);
	}

	@Override
	public void error(String tag, String message) {
		log("ERR", tag, message);
	}
}
