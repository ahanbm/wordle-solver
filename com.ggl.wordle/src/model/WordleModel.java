package com.ggl.wordle.model;

import static java.lang.Double.NaN;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import com.ggl.wordle.controller.ReadWordsRunnable;

public class WordleModel {

	private char[] currentWord, guess;
	private final Solver sol;

	private int columnCount, maximumRows;
	private int currentColumn, currentRow;

	private Map<String, Double> wordMap;

	private List<String> wordList;
	private List<String> guessList;

	private final Random random;
	private final Statistics statistics;

	private WordleResponse[][] wordleGrid;
	private boolean playMode;

	public WordleModel() {
		this.currentColumn = 0;
		this.currentRow = 0;

		try {
			readConfig("config.txt");
		} catch (IOException ie) {
			System.err.println("config.txt not found.");
			System.exit(1);
		}

		this.random = new Random();

		sol = new Solver(columnCount);
		createWordList();

		this.wordleGrid = initializeWordleGrid();
		this.guess = new char[columnCount];
		this.statistics = new Statistics();

		if (playMode) {
			currentResponseColumn = columnCount;
		} else {
			currentResponseColumn = 0;
		}
	}

	private void readConfig(String text) throws IOException {
		File f = new File(text);

		if (f.exists()) {
			try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
				String line = reader.readLine();
				this.columnCount = Integer.parseInt(line);

				line = reader.readLine();
				this.maximumRows = Integer.parseInt(line);

				line = reader.readLine();

				if (line.equals("P") || line.equals("p") || line.equals("Play") || line.equals("play")) {
					this.playMode = true;
				} else {
					this.playMode = false;
				}
			}
		} else {
			throw new IOException("Resource not found: " + text);
		}
	}

	private void createWordList() {
		ReadWordsRunnable runnable = new ReadWordsRunnable(this);
		runnable.run();
	}

	private String guessAsString() {
		StringBuilder s = new StringBuilder();

		for (char c : guess) {
			s.append(c);
		}

		return s.toString();
	}

	public String cheat() {
		StringBuilder word = new StringBuilder();

		for (char c : currentWord) {
			word.append(c);
		}

		return word.toString();
	}

	public void initialize() {
		this.wordleGrid = initializeWordleGrid();
		this.currentColumn = 0;
		this.currentRow = 0;
		generateWord();
		this.guess = new char[columnCount];
	}

	public void generateWord() {
		String word = getWord();
		this.currentWord = word.toUpperCase().toCharArray();
	}

	private String getWord() {
		double total = 0;

		for (double value : wordMap.values()) {
			total += value;
		}

		int randomNumber = random.nextInt((int) total);

		double current = 0;

		for (Map.Entry<String, Double> entry : wordMap.entrySet()) {
			current += entry.getValue();

			if (current >= randomNumber) {
				return entry.getKey();
			}
		}

		return null;
	}

	private WordleResponse[][] initializeWordleGrid() {
		WordleResponse[][] wordleGrid = new WordleResponse[maximumRows][columnCount];

		for (int row = 0; row < wordleGrid.length; row++) {
			for (int column = 0; column < wordleGrid[row].length; column++) {
				wordleGrid[row][column] = null;
			}
		}

		return wordleGrid;
	}

	public void setWordMap(Map<String, Double> map) {
		this.wordMap = map;
		this.wordList = map.keySet().stream().toList();

		sol.setWordMap(map);
	}

	public void setGuessList(List<String> guessList) {
		this.guessList = guessList;
	}

	public void setCurrentColumn(char c) {
		guess[currentColumn] = c;
		wordleGrid[currentRow][currentColumn] = new WordleResponse(c,
				Color.WHITE, Color.BLACK);
		currentColumn++;
	}

	public void backspace() {
		this.currentColumn--;
		this.currentColumn = Math.max(currentColumn, 0);
		wordleGrid[currentRow][currentColumn] = null;
		guess[currentColumn] = ' ';
	}

	public WordleResponse[] getCurrentRow() {
		return wordleGrid[getCurrentRowNumber()];
	}

	public int getCurrentRowNumber() {
		return currentRow - 1;
	}

	private int indexOfFirstMatch(char[] word, char letter, boolean[] matchedIndices) {
		for (int i = 0; i < word.length; i++) {
			if (word[i] == letter && !matchedIndices[i]) {
				return i;
			}
		}
		return -1;
	}

	public Map<String, Double> validWords() {
		// Map<String, Double> result =
		// new TreeMap<>(((o1, o2) -> Double.compare(sol.utility(o1),
		// sol.utility(o2))));
		Map<String, Double> result = new TreeMap<>(
				((o1, o2) -> Double.compare(sol.simpleUtility(o1), sol.simpleUtility(o2))));

		for (String word : wordList) {
			if (sol.works(word)) {
				// if (Double.isNaN(sol.utility(word))) {
				// continue;
				// }
				// result.put(word, sol.utility(word));
				result.put(word, sol.simpleUtility(word));
			}
		}

		return result;
	}

	public Color[] response(char[] guess, char[] answer) {
		Color[] result = new Color[columnCount];
		boolean[] matchedIndices = new boolean[columnCount];

		Arrays.fill(result, AppColors.GRAY);

		for (int column = 0; column < guess.length; column++) {
			if (guess[column] == answer[column]) {
				result[column] = AppColors.GREEN;
				matchedIndices[column] = true;
			}
		}

		for (int column = 0; column < guess.length; column++) {
			int index = indexOfFirstMatch(answer, guess[column], matchedIndices);

			if (index != -1 && result[column] != AppColors.GREEN) {
				result[column] = AppColors.YELLOW;
				matchedIndices[index] = true;
			}
		}

		return result;
	}

	public void resetSolver() {
		sol.reset(columnCount);
	}

	public boolean guessIsValid() {
		String s = guessAsString();
		s = s.toLowerCase();

		return wordList.contains(s) || guessList.contains(s);
	}

	public boolean setCurrentRow(char[] chars) {
		Color[] toSet;

		if (playMode) {
			toSet = response(guess, currentWord);
		} else {
			toSet = new Color[chars.length];

			for (int i = 0; i < toSet.length; ++i) {
				if (chars[i] == 'g') {
					toSet[i] = AppColors.GREEN;
				} else if (chars[i] == 'y') {
					toSet[i] = AppColors.YELLOW;
				} else {
					toSet[i] = AppColors.GRAY;
				}
			}
		}

		sol.log(guess, toSet);

		Color foregroundColor = Color.WHITE;

		for (int column = 0; column < guess.length; column++) {
			Color backgroundColor = toSet[column];

			wordleGrid[currentRow][column] = new WordleResponse(guess[column],
					backgroundColor, foregroundColor);
		}

		currentColumn = 0;
		currentRow++;

		guess = new char[columnCount];

		boolean more = currentRow < maximumRows;

		if (!more) {
			resetSolver();
		}

		return more;
	}

	public WordleResponse[][] getWordleGrid() {
		return wordleGrid;
	}

	public int getMaximumRows() {
		return maximumRows;
	}

	public int getColumnCount() {
		return columnCount;
	}

	public int getCurrentColumn() {
		return currentColumn;
	}

	public int getTotalWordCount() {
		return wordList.size();
	}

	public Statistics getStatistics() {
		return statistics;
	}
}