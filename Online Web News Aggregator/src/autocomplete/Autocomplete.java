package autocomplete;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * @author alonb
 *
 */
public class Autocomplete implements IAutocomplete {

	private Node root;

	@Override
	public void addWord(String word, long weight) {
		if (word.matches("[a-zA-Z]+")) {
			word = word.toLowerCase();
			root = addWord(root, word, weight, word);
		}
	}

	// addWord helper method to insert word to Trie recursively. node is the Node to
	// insert from and cutWord is a helper parameter that cuts the word one
	// character from the left at each recursive call.
	private Node addWord(Node node, String word, long weight, String cutWord) {
		if (node == null)
			node = new Node();
		if (cutWord.isEmpty()) {
			Node tmp = new Node(word, weight);
			node.setPrefixes(node.getPrefixes() + 1);
			node.setWords(node.getWords() + 1);
			node.setTerm(tmp.getTerm());
			return node;
		}
		if (node.isLeaf())
			node.setReferences(new Node[26]);
		node.setPrefixes(node.getPrefixes() + 1);
		int pos = cutWord.charAt(0) - 'a';
		node.getReferences()[pos] = addWord(node.getReferences()[pos], word, weight,
				cutWord.substring(1, cutWord.length()));
		return node;
	}

	@Override
	public Node buildTrie(String filename, int k) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String line = br.readLine(); // get rid of first line.
			line = br.readLine();
			line = line.replace(" ", "");
			String[] lineSplit = line.split("\t");
			while (lineSplit.length == 2) {
				if (lineSplit[1].matches("[a-zA-Z]+"))
					addWord(lineSplit[1], Long.valueOf(lineSplit[0]));
				line = br.readLine();
				if (line == null)
					break;
				line = line.replace(" ", "");
				lineSplit = line.split("\t");
			}
			br.close();
			return root;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public int numberSuggestions() {
		return 0; // AutocompleteGUI gets this parameter from the integer inserted after the path.
	}

	@Override
	public Node getSubTrie(String prefix) {
		prefix = prefix.toLowerCase();
		return traverseByPrefix(prefix);
	}

	@Override
	public int countPrefixes(String prefix) {
		prefix = prefix.toLowerCase();
		Node node = traverseByPrefix(prefix);
		if (node == null)
			return 0;
		return node.getPrefixes();
	}

	@Override
	public List<ITerm> getSuggestions(String prefix) { // bfs-traversal of the Trie from the prefix point.
		prefix = prefix.toLowerCase();
		List<ITerm> ret = new ArrayList<ITerm>();
		Node node = traverseByPrefix(prefix);
		if (node == null)
			return ret;
		Queue<Node> Q = new LinkedList<Node>();
		Q.add(node);
		while (!Q.isEmpty()) {
			Node tmp = Q.remove();
			if (tmp.getWords() > 0)
				ret.add(tmp.getTerm());
			if (tmp.isLeaf())
				continue;
			for (int i = 0; i < tmp.getReferences().length; i++)
				if (tmp.getReferences()[i] != null)
					Q.add(tmp.getReferences()[i]);
		}
		ret.sort(ITerm.byPrefixOrder(Integer.MAX_VALUE));
		return ret;
	}

	// helper method to getSubTrie, countPrefixes and getSuggestions that traverses
	// the Trie based on prefix and returns the node it corresponds to.
	private Node traverseByPrefix(String prefix) {
		if (!prefix.isEmpty() && !prefix.matches("[a-zA-Z]+"))
			return null;
		Node tmp = root;
		int pos;
		for (int i = 0; i < prefix.length(); i++) {
			pos = prefix.charAt(i) - 'a';
			if (tmp.isLeaf() || tmp.getReferences()[pos] == null)
				return null;
			tmp = tmp.getReferences()[pos];
		}
		return tmp;
	}

}
