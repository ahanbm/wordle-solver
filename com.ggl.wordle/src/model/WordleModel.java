package com.ggl.wordle.model;

import java.awt.Color;
import java.util.List;
import java.util.Random;

import com.ggl.wordle.controller.ReadWordsRunnable;

public class WordleModel {

	private char[] currentWord, guess;

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

		this.wordleGrid = initializeWordleGrid();
		this.guess = new char[columnCount];
		this.statistics = new Statistics();
	}

	private void createWordList() {
		ReadWordsRunnable runnable = new ReadWordsRunnable(this);
		new Thread(runnable).start();
	}

	public String cheat() {
		StringBuilder s = new StringBuilder();

		for (char c : currentWord) {
			s.append(c);
		}

		return s.toString();
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
		currentColumn = Math.min(currentColumn, (columnCount - 1));
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

	public boolean setCurrentRow() {
		boolean[] matchedIndices = new boolean[currentWord.length];

		for (int column = 0; column < guess.length; column++) {
			Color backgroundColor = AppColors.GRAY;
			Color foregroundColor = Color.WHITE;

			if (guess[column] == currentWord[column]) {
				backgroundColor = AppColors.GREEN;
				matchedIndices[column] = true;
			}

			wordleGrid[currentRow][column] = new WordleResponse(guess[column],
					backgroundColor, foregroundColor);
		}

		for (int column = 0; column < guess.length; column++) {
			Color backgroundColor = AppColors.GRAY;
			Color foregroundColor = Color.WHITE;
			if (guess[column] == currentWord[column]) {
				continue;
			}

			int index = indexOfFirstMatch(currentWord, guess[column], matchedIndices);
			if (index != -1) {
				backgroundColor = AppColors.YELLOW;
				matchedIndices[index] = true;
			}

			wordleGrid[currentRow][column] = new WordleResponse(guess[column],
					backgroundColor, foregroundColor);
		}

		currentColumn = 0;
		currentRow++;
		guess = new char[columnCount];

		return currentRow < maximumRows;
	}

	private boolean contains(char[] currentWord, char[] guess, int column) {
		for (int index = 0; index < currentWord.length; index++) {
			if (index != column && guess[column] == currentWord[index]) {
				return true;
			}
		}

		return false;
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
