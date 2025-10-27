package app.parsing.css.properties.tokenizers

/**
 * Applies tokenizers hierarchically to preserve grouping structure.
 * Returns List<List<String>> where each inner list is a group from the first tokenizer,
 * and subsequent tokenizers are applied to each group.
 */
object CompositeTokenizer {
    
    /**
     * Apply tokenizers hierarchically.
     * 
     * Example: tokenize("a b, c d", [CT, WT])
     *   → CT creates: ["a b", "c d"]
     *   → WT on each: [["a", "b"], ["c", "d"]]
     */
    fun tokenize(input: String, tokenizers: List<Tokenizer>): List<List<String>> {
        if (tokenizers.isEmpty()) {
            return listOf(listOf(input))
        }
        
        // First tokenizer creates groups
        val groups = tokenizers.first().tokenize(input)
        
        if (tokenizers.size == 1) {
            // Only one tokenizer - return each token as a single-item group
            return groups.map { listOf(it) }
        }
        
        // Apply remaining tokenizers to each group
        val remainingTokenizers = tokenizers.drop(1)
        return groups.map { group ->
            applyTokenizers(group, remainingTokenizers)
        }
    }
    
    private fun applyTokenizers(input: String, tokenizers: List<Tokenizer>): List<String> {
        var results = listOf(input)
        for (tokenizer in tokenizers) {
            results = results.flatMap { tokenizer.tokenize(it) }
        }
        return results.filter { it.isNotBlank() }
    }
}