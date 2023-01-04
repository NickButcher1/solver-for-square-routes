package squareroutes

/**
 * Tracks a possible state of the grid, with a list of possible letters for each cell.
 */
data class EmptyGrid(
    val possibleLetters: List<List<List<Char>>>
) {
    val canMoveTo: MutableList<List<List<Pair<Int, Int>>>> = mutableListOf()

    init {
        val numRows = possibleLetters.size
        val numCols = possibleLetters[0].size

        for (r in (0 until numRows)) {

            val canMoveToForRow: MutableList<List<Pair<Int, Int>>> = mutableListOf()
            canMoveTo.add(canMoveToForRow)
            for (c in (0 until numCols)) {
                val canMoveToForCell: MutableList<Pair<Int, Int>> = mutableListOf()
                canMoveToForRow.add(canMoveToForCell)
                if (r != 0) {
                    canMoveToForCell.add(Pair(r - 1, c))
                }
                if (r != (numRows - 1)) {
                    canMoveToForCell.add(Pair(r + 1, c))
                }
                if (c != 0) {
                    canMoveToForCell.add(Pair(r, c - 1))
                }
                if (c != (numCols - 1)) {
                    canMoveToForCell.add(Pair(r, c + 1))
                }
            }
        }
    }
}
