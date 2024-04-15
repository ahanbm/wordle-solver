package com.ggl.wordle.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.File;
import java.io.FileReader;

import com.ggl.wordle.model.WordleModel;

public class ReadWordsRunnable implements Runnable {

	private final static Logger LOGGER = Logger.getLogger(ReadWordsRunnable.class.getName());

	private final WordleModel model;

	public ReadWordsRunnable(WordleModel model) {
		LOGGER.setLevel(Level.INFO);

		try {
			FileHandler fileTxt = new FileHandler("./logging.txt");
			LOGGER.addHandler(fileTxt);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.model = model;
	}

	@Override
	public void run() {
		List<String> wordlist;

		try {
			wordlist = createWordList();
			LOGGER.info("Created word list of " + wordlist.size() + " words.");
		} catch (IOException e) {
			LOGGER.info(e.getMessage());
			e.printStackTrace();
			wordlist = new ArrayList<>();
		}

		model.setWordList(wordlist);
		model.generateCurrentWord();
	}

	private List<String> createWordList() throws IOException {
		int minimum = model.getColumnCount();

		List<String> wordlist = new ArrayList<>();

		File directory = new File("resources/");

		// Check if the directory exists
		if (directory.exists() && directory.isDirectory()) {
			// List the contents of the directory
			File[] files = directory.listFiles();
			if (files != null) {
				System.out.println("Contents of ../:");
				for (File file : files) {
					System.out.println(file.getName());
				}
			} else {
				System.out.println("Failed to list directory contents.");
			}
		} else {
			System.out.println("Directory ../ does not exist or is not a directory.");
		}

		String text = "resources/usa.txt";
		File f = new File(text);

		if (f.exists()) {
			try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
				String line = reader.readLine();
				while (line != null) {
					line = line.trim();
					if (line.length() == minimum) {
						wordlist.add(line);
					}
					line = reader.readLine();
				}
			}
		} else {
			throw new IOException("Resource not found: " + text);
		}

		return wordlist;
	}

}
