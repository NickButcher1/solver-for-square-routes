Solver for the Square Routes puzzle which is published in The Times (UK) newspaper.

# Inputs

Inputs are stored in `input/<name>`, one file per puzzle.

File format is:

- The list of words, in any order.
- A blank line.
- The grid, where V represents an initial vowel, v a subsequent vowel, C an initial consonant and c a subsequent consonant.

# Running the Solver

- `gradlew run --args "<name>"` to solve a single puzzle and print the results to stdout.
