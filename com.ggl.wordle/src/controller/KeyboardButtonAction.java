package com.ggl.wordle.controller;

import java.awt.event.ActionEvent;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JButton;

import com.ggl.wordle.model.AppColors;
import com.ggl.wordle.model.WordleModel;
import com.ggl.wordle.model.WordleResponse;
import com.ggl.wordle.view.StatisticsDialog;
import com.ggl.wordle.view.WordleFrame;

public class KeyboardButtonAction extends AbstractAction {

	private static final long serialVersionUID = 1L;

	private final WordleFrame view;

	private final WordleModel model;

	public KeyboardButtonAction(WordleFrame view, WordleModel model) {
		this.view = view;
		this.model = model;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		JButton button = (JButton) event.getSource();
		String text = button.getActionCommand();
		switch (text) {
			case "Enter":
				if (model.getCurrentColumn() == (model.getColumnCount())) {
					if (!model.guessIsValid()) {
						break;
					}

					boolean moreRows = model.setCurrentRow();
					WordleResponse[] currentRow = model.getCurrentRow();
					int greenCount = 0;
					for (WordleResponse wordleResponse : currentRow) {
						view.setColor(Character.toString(
								wordleResponse.getChar()),
								wordleResponse.getBackgroundColor(),
								wordleResponse.getForegroundColor());
						if (wordleResponse.getBackgroundColor()
								.equals(AppColors.GREEN)) {
							greenCount++;
						}
					}

					if (greenCount >= model.getColumnCount()) {
						System.out.println("You Won!");
						view.repaintWordleGridPanel();
						model.getStatistics().incrementTotalGamesPlayed();
						int currentRowNumber = model.getCurrentRowNumber();
						model.getStatistics()
								.addWordsGuessed(currentRowNumber);
						int currentStreak = model.getStatistics()
								.getCurrentStreak();
						model.getStatistics()
								.setCurrentStreak(++currentStreak);
						new StatisticsDialog(view, model);
						model.resetSolver();
					} else if (!moreRows) {
						System.out.println("You lost!");
						view.repaintWordleGridPanel();
						model.getStatistics().incrementTotalGamesPlayed();
						model.getStatistics().setCurrentStreak(0);
						new StatisticsDialog(view, model);
						model.resetSolver();
					} else {
						for (Map.Entry<String, Double> entry : model.validWords().entrySet()) {
							String key = entry.getKey();
							Double value = entry.getValue();

							System.out.println(key + " " + value);
						}

						System.out.println("Printing Complete");

						for (int i = 0; i < 5; ++i) {
							System.out.println();
						}

						view.repaintWordleGridPanel();
					}
				}
				break;
			case "Backspace":
				model.backspace();
				view.repaintWordleGridPanel();
				break;
			default:
				if (model.getCurrentColumn() != (model.getColumnCount())) {
					model.setCurrentColumn(text.charAt(0));
					view.repaintWordleGridPanel();
				}

				break;
		}

	}
}