# Wordle

This repository is forked from https://github.com/ggleblanc2/wordle, for an
explanation of the classes please refer to the documentation of that. The 
license of this project is also shared. This project has two main contributions, 
described below. 

## Configurations

This projects allow configuration of 5 main things: the number of letters in
each word, the number of allowed guesses, the game mode, the word set, and
the prior (probability of being selected as the correct word) of each word
in the word set. 

The first three of these can be set in config.txt. The first row is the
number of letters, the second row is the number of guesses, and the third is
the game mode (P for Play and S for Solve)

In the Play game mode, the program will select a correct word and you will
attempt to guess it, with or without the assistance of the solver. In the Solve
game mode you can input responses and guesses from a third party platform by
putting the response in resources/response.txt (put 1 character for each letter
in the guess, with g corresponding to green, y to yellow, and r to gray). The
Solver will then assist with a solution. 

To change the word set, change words.txt and frequency.txt. If you want to use
a different prior generation method than in FrequencyAPI, put the custom
frequencies in frequency.txt. 

## Solver

A solver class is implemented with flexible structure to allow for easy 
improvements. The demo version that is currently activated is very fast and 
simple, as well as being relatively good for solving. 

To use the solver most effectively, in Play mode, have the terminal open and
the game so you can see both at once. Always select the word from the solver
that has the highest utility (prints at the bottom). In Solve method, both of
these still apply but additionally keep resources/response.txt open in a
seperate window and edit the response letters accordingly. 