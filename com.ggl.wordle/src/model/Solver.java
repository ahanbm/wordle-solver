package com.ggl.wordle.model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class Solver {
    private boolean[][] possible;
    private int[] yellows;
    private List<Integer> greenIndexes;
    private Map<Character, Integer> numChars;

    public Solver(int letters) {
        reset(letters);
    }

    public void reset(int letters) {
        possible = new boolean[letters][26];
        yellows = new int[26];
        greenIndexes = new ArrayList<>();
        numChars = new HashMap<>();

        for (int i = 0; i < letters; ++i) {
            for (int j = 0; j < 26; ++j) {
                possible[i][j] = true;
            }
        }
    }

    private int numRepeats(char c) {
        int total = yellows[(int)c - 65];

        for (int index : greenIndexes) {
            if (greenIndexes.get(index) == c) {
                ++total;
            }
        }

        return total;
    }

    private int numRepeats(char[] characters, char c) {
        int total = 0;

        for (char ch : characters) {
            if (ch == c) {
                ++total;
            }
        }

        return total;
    }

    public void log(char[] guess, Color[] response) {
        for (int i = 0; i < guess.length; ++i) {
            if (response[i].equals(AppColors.GREEN)) {
                for (int j = 0; j < 26; ++j) {
                    if (j != (int)(guess[i]) - 65) {
                        possible[i][j] = false;
                    }
                }

                greenIndexes.add(i);
            }
        }

        int[] newYellows = new int[26];

        for (int i = 0; i < guess.length; ++i) {
            if (response[i].equals(AppColors.YELLOW)) {
                possible[i][(int)guess[i] - 65] = false;
                ++newYellows[(int)guess[i] - 65];
            }
        }

        for (int i = 0; i < guess.length; ++i) {
            if (response[i].equals(AppColors.GRAY)) {
                if (yellows[(int) guess[i] - 65] > 0) {
                    numChars.put(guess[i], numRepeats(guess[i]));
                    possible[i][(int) guess[i] - 65] = false;
                } else {
                    for (int j = 0; j < possible.length; ++j) {
                        if (!greenIndexes.contains(j)) {
                            possible[j][(int) guess[i] - 65] = false;
                        }
                    }
                }
            }
        }

        for (int i = 0; i < 26; ++i) {
            if (newYellows[i] > yellows[i]) {
                yellows[i] = newYellows[i];
            }
        }
    }

    private int indexOf(char[] characters, char c, boolean[] matched) {
        for (int i = 0; i < characters.length; ++i) {
            if (characters[i] == c && !matched[i]) {
                return i;
            }
        }

        return -1;
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

        boolean[] matched = new boolean[possible.length];

        for (int i = 0; i < 26; ++i) {
            if (yellows[i] == 0) {
                continue;
            }

            char c = (char)(i + 65);
            int val = indexOf(characters, c, matched);

            if (val == -1) {
                return false;
            }

            matched[val] = true;
        }

        for (Map.Entry<Character, Integer> entry : numChars.entrySet()) {
            Character key = entry.getKey();
            Integer value = entry.getValue();

            if (numRepeats(characters, key) != value) {
                return false;
            }
        }

        return true;
    }
}
