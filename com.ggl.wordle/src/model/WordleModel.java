package com.ggl.wordle.model;

import java.awt.Color;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Arrays;

import com.ggl.wordle.controller.ReadWordsRunnable;

public class WordleModel {

	private char[] currentWord, guess;
	private final Solver sol;

	private final int columnCount, maximumRows;
	private int currentColumn, currentRow;

	private List<String> wordList;

	private final Random random;

	private final Statistics statistics;

	private WordleResponse[][] wordleGrid;

	public WordleModel() {
		this.currentColumn = 0;
		this.currentRow = 0;
		this.columnCount = 5;
		this.maximumRows = 6;
		this.random = new Random();

		createWordList();
		sol = new Solver(columnCount);

		this.wordleGrid = initializeWordleGrid();
		this.guess = new char[columnCount];
		this.statistics = new Statistics();
	}

	private void createWordList() {
		ReadWordsRunnable runnable = new ReadWordsRunnable(this);
		new Thread(runnable).start();
	}

	public String guessAsString() {
		StringBuilder s = new StringBuilder();

		for (char c : currentWord) {
			s.append(c);
		}

		return s.toString();
	}

	public String cheat() {
		return guessAsString();
	}

	public void initialize() {
		this.wordleGrid = initializeWordleGrid();
		this.currentColumn = 0;
		this.currentRow = 0;
		generateCurrentWord();
		this.guess = new char[columnCount];
	}

	public void generateCurrentWord() {
		String word = getCurrentWord();
		this.currentWord = word.toUpperCase().toCharArray();
	}

	private String getCurrentWord() {
		return wordList.get(getRandomIndex());
	}

	private int getRandomIndex() {
		int size = wordList.size();
		return random.nextInt(size);
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

	public void setWordList(List<String> wordList) {
		this.wordList = wordList;
	}

	public void setCurrentWord() {
		int index = getRandomIndex();
		currentWord = wordList.get(index).toCharArray();
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

	public List<String> validWords() {
		ArrayList<String> result = new ArrayList<>();

		for (String word : wordList) {
			if (sol.works(word)) {
				result.add(word);
			}
		}

		return result;
	}

	public Color[] response(char[] guess) {
		Color[] result = new Color[currentWord.length];
		boolean[] matchedIndices = new boolean[currentWord.length];

		Arrays.fill(result, AppColors.GRAY);

		for (int column = 0; column < guess.length; column++) {
			if (guess[column] == currentWord[column]) {
				result[column] = AppColors.GREEN;
				matchedIndices[column] = true;
			}
		}

		for (int column = 0; column < guess.length; column++) {
			int index = indexOfFirstMatch(currentWord, guess[column], matchedIndices);

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

	public boolean guessIsWord() {
		return wordList.contains(guessAsString());
	}

	public boolean setCurrentRow() {
		Color[] toSet = response(guess);
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