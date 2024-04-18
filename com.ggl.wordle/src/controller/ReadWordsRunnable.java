package com.ggl.wordle.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
		} catch (IOException | SecurityException e) {
			e.printStackTrace();
		}

		this.model = model;
	}

	@Override
	public void run() {
		Map<String, Double> wordMap;
		List<String> guessList;

		try {
			wordMap = createWordList("resources/frequency.txt");
			guessList = createGuessList("resources/guesses.txt");

			LOGGER.info("Created word list of " + wordMap.size() + " words.");
		} catch (IOException e) {
			LOGGER.info(e.getMessage());
			e.printStackTrace();
			wordMap = new HashMap<>();
			guessList = new ArrayList<>();
		}

		model.setWordMap(wordMap);
		model.setGuessList(guessList);

		model.generateWord();
	}

	private Map<String, Double> createWordList(String text) throws IOException {
		int length = model.getColumnCount();

		Map<String, Double> result = new HashMap<>();
		File f = new File(text);

		if (f.exists()) {
			try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
				String line = reader.readLine();
				while (line != null) {
					String[] parts = line.split("\\s+");
					String str = parts[0];
					double num = Double.parseDouble(parts[1]);

					if (str.length() == length) {
						result.put(str, num);
					}

					line = reader.readLine();
				}
			}
		} else {
			throw new IOException("Resource not found: " + text);
		}

		return result;
	}

	private List<String> createGuessList(String text) throws IOException {
		int minimum = model.getColumnCount();

		List<String> wordlist = new ArrayList<>();
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
