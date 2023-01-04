package squareroutes

import java.io.File
import kotlin.system.exitProcess

class Solver(inputFilename: String) {
    private val inputLines = File(inputFilename).useLines { it.toList() }

    private val words: List<String> =
        inputLines.takeWhile { line -> line.isNotEmpty() }
            .map { line -> line }

    private val gridLines: List<String> =
        inputLines.takeLastWhile { line -> line.isNotEmpty() }.map { it }

    private val rows = gridLines.size
    private val cols = gridLines[0].length

    private val initialVowelWords: List<String> =
        words.filter { word -> word[0] in VOWELS }

    private val initialConsonantWords: List<String> =
        words.filter { word -> word[0] !in VOWELS }

    /** Unique values, ordered alphabetically for all four of these lists. */
    private val initialVowels: List<Char> =
        words.filter { word -> word[0] in VOWELS }
            .map { word -> word[0] }
            .distinct()
            .toList()

    private val initialConsonants: List<Char> =
        words.asSequence().filter { word -> word[0] !in VOWELS }
            .map { word -> word[0] }
            .distinct()
            .toList()
            .sorted().toList()

    private val subsequentVowels: List<Char> =
        words.joinToString("") { word -> word.subSequence(1, word.length) }
            .toList()
            .filter { char -> char in VOWELS }
            .distinct()
            .sorted()

    private val subsequentConsonants: List<Char> =
        words.joinToString("") { word -> word.subSequence(1, word.length) }
            .toList()
            .filter { char -> char !in VOWELS }
            .distinct()
            .sorted()

    private val initialVowelCells: MutableList<Pair<Int, Int>> = mutableListOf()

    private val initialConsonantCells: MutableList<Pair<Int, Int>> = mutableListOf()

    private val emptyGrid: EmptyGrid = buildEmptyGrid()

    private fun buildEmptyGrid(): EmptyGrid {
        val possibleLetters = gridLines.mapIndexed { row, gridLine ->
            gridLine.mapIndexed { col, gridChar ->
                when (gridChar) {
                    'v' -> subsequentVowels
                    'V' -> {
                        initialVowelCells.add(Pair(row, col))
                        initialVowels
                    }
                    'c' -> subsequentConsonants
                    'C' -> {
                        initialConsonantCells.add(Pair(row, col))
                        initialConsonants
                    }
                    else ->
                        throw IllegalArgumentException("Unexpected character in grid input.")
                }
            }
        }
        return EmptyGrid(possibleLetters)
    }

    private fun mapOfPlaced(word: String, cellsPlacedAlready: List<Pair<Int, Int>>): Map<Pair<Int, Int>, Char> {
        val lettersPlacedAlready: MutableMap<Pair<Int, Int>, Char> = mutableMapOf()
        word.forEachIndexed { index, c ->
            when (lettersPlacedAlready[cellsPlacedAlready[index]]) {
                null -> {
                    lettersPlacedAlready[cellsPlacedAlready[index]] = c
                }
                c -> {
                    // OK
                }
                else -> {
                    // Unexpected.
                    println("ERROR placing $index $c in ${cellsPlacedAlready[index]}, ALREADY $lettersPlacedAlready")
                    exitProcess(0)
                }
            }
        }
        return lettersPlacedAlready
    }
    private fun findAllPaths(
        initialCells: List<Pair<Int, Int>>,
        initialWords: List<String>
    ): MutableMap<String, MutableList<List<Pair<Int, Int>>>> {
        val pathsMap: MutableMap<String, MutableList<List<Pair<Int, Int>>>> = mutableMapOf()

        initialWords.forEach { word ->
            pathsMap[word] = mutableListOf()

            initialCells.forEach { cell ->
                var depth = 1
                var optionsAtDepth: MutableList<List<Pair<Int, Int>>> = mutableListOf()
                optionsAtDepth.add(mutableListOf(cell))

                while (depth < word.length) {
                    depth += 1
                    val letter = word[depth - 1]
                    val optionsAtNewDepth: MutableList<List<Pair<Int, Int>>> = mutableListOf()
                    optionsAtDepth.forEach { option ->
                        val placedAlready = mapOfPlaced(word.substring(0, depth - 1), option)
                        val lastTime = option.last()
                        val nextCells = emptyGrid.canMoveTo[lastTime.first][lastTime.second]
                        nextCells.forEach { nextCell ->
                            if (letter in emptyGrid.possibleLetters[nextCell.first][nextCell.second]) {
                                val alreadyPlacedLetter = placedAlready[Pair(nextCell.first, nextCell.second)]
                                if (alreadyPlacedLetter == null || alreadyPlacedLetter == letter) {
                                    val newLastTime = option.toMutableList()
                                    newLastTime.add(nextCell)
                                    optionsAtNewDepth.add(newLastTime)
                                } else {
                                    // Reject because a different letter is in the cell.
                                }
                            }
                        }
                    }
                    optionsAtDepth = optionsAtNewDepth
                }
                pathsMap[word]!!.addAll(optionsAtDepth)
            }
        }

        return pathsMap
    }

    /**
     * If possible, place the specified word into placedMap and return true, otherwise return
     * false.
     */
    private fun placeWord(
        word: String,
        path: List<Pair<Int, Int>>,
        placedMap: MutableMap<Pair<Int, Int>, Char>
    ): Boolean {
        var canPlace = true

        word.forEachIndexed { index, c ->
            if (placedMap[path[index]] != null && placedMap[path[index]] != c) {
                canPlace = false
            }
        }

        if (canPlace) {
            word.forEachIndexed { index, c ->
                placedMap[path[index]] = c
            }
        }

        return canPlace
    }

    private fun printSolution(
        placedMap: MutableMap<Pair<Int, Int>, Char>
    ) {
        for (i in 0 until rows) {
            print("\n")
            for (j in 0 until cols) {
                print(placedMap[Pair(i, j)])
            }
        }
        print("\n")
    }

    private fun solveWithPathsToDepth(
        depth: Int,
        paths: Map<String, MutableList<List<Pair<Int, Int>>>>,
        placedMap: MutableMap<Pair<Int, Int>, Char>
    ) {
        val word = words[depth]
        val pathsForWord = paths[word]!!
        pathsForWord.forEach { path ->
            val newPlacedMap = placedMap.toMutableMap() // Copy the map.

            if (placeWord(word, path, newPlacedMap)) {
                if (depth == (words.size - 1)) {
                    printSolution(newPlacedMap)
                    exitProcess(0)
                } else {
                    solveWithPathsToDepth(depth + 1, paths, newPlacedMap)
                }
            }
        }
    }

    fun solve() {
        val vowelsPathsMap = findAllPaths(initialVowelCells, initialVowelWords)
        val consonantsPathsMap = findAllPaths(initialConsonantCells, initialConsonantWords)
        solveWithPathsToDepth(0, vowelsPathsMap + consonantsPathsMap, mutableMapOf())
    }

    companion object {
        private val VOWELS = listOf('A', 'E', 'I', 'O', 'U')
    }
}
