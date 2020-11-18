package autocomplete;


/**
 * ==== Attributes ==== - words: number of words - term: the ITerm object -
 * prefixes: number of prefixes - references: Array of references to
 * next/children Nodes
 * 
 * ==== Constructor ==== Node(String word, long weight)
 * 
 * @author alonb
 */
public class Node {
	private int words;
	private Term term;
	private int prefixes;
	private Node[] references;

	/**
	 * Create a new Node object.
	 */
	public Node() {

	}

	/**
	 * Create a new Node object.
	 * 
	 * @param word   the word to be added to the Node
	 * @param weight the weight of the word
	 */
	public Node(String word, long weight) {
		this.term = new Term(word, weight);
	}

	/**
	 * @return the words
	 */
	public int getWords() {
		return words;
	}

	/**
	 * @param words the words to set
	 */
	public void setWords(int words) {
		this.words = words;
	}

	/**
	 * @return the term
	 */
	public Term getTerm() {
		return term;
	}

	/**
	 * @param term the term to set
	 */
	public void setTerm(Term term) {
		this.term = term;
	}

	/**
	 * @return the prefixes
	 */
	public int getPrefixes() {
		return prefixes;
	}

	/**
	 * @param prefixes the prefixes to set
	 */
	public void setPrefixes(int prefixes) {
		this.prefixes = prefixes;
	}

	/**
	 * tells us if the node is an external node or not
	 * 
	 * @return if the node is a leaf or not
	 */
	public boolean isLeaf() {
		if (this.references == null)
			return true;
		return false;
	}

	/**
	 * @return the references.
	 */
	public Node[] getReferences() {
		return references;
	}

	/**
	 * @param references the references to set
	 */
	public void setReferences(Node[] references) {
		this.references = references;
	}

	public String toString() {
		return term.toString();
	}
}
