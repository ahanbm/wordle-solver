package com.ggl.wordle.model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;

public class Solver {
    private boolean[][] possible;
    private int[] yellows;

    private List<Integer> greenIndexes;
    private Map<Character, Integer> numChars;

    private Map<String, Double> wordMap;

    public Solver(int letters) {
        wordMap = new HashMap<>();
        reset(letters);
    }

    public Solver(boolean[][] possible, int[] yellows, List<Integer> greenIndexes, Map<Character,
            Integer> numChars, Map<String, Double> wordMap) {
        this.possible = possible;
        this.yellows = yellows;
        this.greenIndexes = greenIndexes;
        this.numChars = numChars;
        this.wordMap = wordMap;
    }

    public void setWordMap(Map<String, Double> wordMap) {
        this.wordMap = wordMap;
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

        for (Integer green : greenIndexes) {
            if (green == ((int) c - 65)) {
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

    public List<String> validWords() {
        List<String> result = new ArrayList<>();

        for (String word : wordMap.keySet()) {
            if (works(word)) {
                result.add(word);
            }
        }

        return result;
    }

    private int indexOfFirstMatch(char[] word, char letter, boolean[] matchedIndices) {
        for (int i = 0; i < word.length; i++) {
            if (word[i] == letter && !matchedIndices[i]) {
                return i;
            }
        }
        return -1;
    }

    public Color[] response(char[] guess, char[] answer) {
        Color[] result = new Color[possible.length];
        boolean[] matchedIndices = new boolean[possible.length];

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

    public static boolean[][] deepCopy(boolean[][] original) {
        int rows = original.length;
        int columns = original[0].length;
        boolean[][] copy = new boolean[rows][columns];

        for (int i = 0; i < rows; i++) {
            System.arraycopy(original[i], 0, copy[i], 0, columns);
        }

        return copy;
    }

    public double simpleUtility(String word) {
        return wordMap.get(word);
    }

    public double utility(String word) {
        double totalProp = 0;
        List<String> words = validWords();

        double totalWeight = 0;

        for (String answer : words) {
            Solver sol = new Solver(deepCopy(possible), yellows.clone(),
                    new ArrayList<>(greenIndexes), new HashMap<>(numChars), new HashMap<>(wordMap));

            char[] wordC = word.toUpperCase().toCharArray();
            char[] answerC = answer.toUpperCase().toCharArray();

            sol.log(wordC, response(wordC, answerC));

            double total = 0;
            double max = 0;

            for (String possible: sol.validWords()) {
                double prob = wordMap.get(possible);

                total += prob;
                max = Math.max(max, prob);
            }

            double prop = max / total;
            totalProp += prop;

            totalWeight += wordMap.get(answer);
        }

        double averageProp = totalProp / words.size();
        double currentProp = wordMap.get(word) / totalWeight;

        return currentProp * averageProp;
    }
}
