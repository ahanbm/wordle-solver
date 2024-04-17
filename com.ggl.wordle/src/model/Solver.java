package com.ggl.wordle.model;

import java.awt.Color;
import java.util.ArrayList;

public class Solver {
    private final boolean[][] possible;

    public Solver(int letters) {
        possible = new boolean[letters][26];

        for (int i = 0; i < letters; ++i) {
            for (int j = 0; j < 26; ++j) {
                possible[i][j] = true;
            }
        }
    }

    public void log(char[] guess, Color[] response) {
        ArrayList<Character> greens = new ArrayList<>();

        for (int i = 0; i < guess.length; ++i) {
            if (response[i].equals(AppColors.GREEN)) {
                for (int j = 0; j < 26; ++j) {
                    if (j != guess[i]) {
                        possible[i][j] = false;
                    }
                }

                greens.add(guess[i]);
            }
        }

        for (int i = 0; i < guess.length; ++i) {
            if (response[i].equals(AppColors.GRAY) && !greens.contains(guess[i])) {
                for (int j = 0; j < possible.length; ++i) {
                    possible[j][(int)guess[i] - 65] = false;
                }
            }
        }
    }

    public boolean works(String word) {
        if (word.length() != possible.length) {
            return false;
        }

        char[] characters = (word.toUpperCase()).toCharArray();

        for (int i = 0; i < characters.length; ++i) {
            if (!possible[i][(int)characters[i] - 65]) {
                return false;
            }
        }

        return true;
    }
}
