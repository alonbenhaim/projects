package indexing;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * @author alonb
 *
 */
public class IndexBuilder implements IIndexBuilder {

	@Override
	public Map<String, List<String>> parseFeed(List<String> feeds) {
		try {
			Map<String, List<String>> ret = new HashMap<>();
			for (String url : feeds) {
				Document rss = Jsoup.connect(url).get();
				Elements links = rss.getElementsByTag("link");
				for (Element link : links) {
					Document doc = Jsoup.connect(link.text()).get();
					Elements body = doc.getElementsByTag("body");
					String text = body.text().replaceAll("[^a-zA-Z0-9\\s+]", "");
					text = text.toLowerCase();
					String split[] = text.split(" ");
					List<String> words = new ArrayList<String>(Arrays.asList(split));
					ret.put(link.text(), words);
				}
			}
			return ret;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Map<String, Map<String, Double>> buildIndex(Map<String, List<String>> docs) {
		Map<String, Map<String, Double>> ret = new HashMap<>();
		List<String> allWords = new ArrayList<>();
		for (Map.Entry<String, List<String>> doc : docs.entrySet()) { // makes a list allWords with every term taken
																		// only once from each document. also calculates
																		// TF for every term.
			Map<String, Double> retTerm = new TreeMap<>();
			List<String> words = doc.getValue();
			int numTerms = words.size();
			Collections.sort(words);
			String next = null;
			int count = 0;
			for (int i = 0; i < words.size(); i++) {
				String term = words.get(i);
				count++;
				if (i != (words.size() - 1)) {
					next = words.get(i + 1);
				}
				if (!term.equals(next) || (i == (words.size() - 1))) {
					double TF = count / ((double) (numTerms));
					retTerm.put(term, TF);
					count = 0;
					allWords.add(term);
				}
			}
			ret.put(doc.getKey(), retTerm);
		}

		int numDocs = docs.size();

		for (Map.Entry<String, List<String>> doc : docs.entrySet()) {
			List<String> words = doc.getValue();
			Map<String, Double> retTerm = ret.get(doc.getKey());
			Map<String, Double> retTerm2 = new TreeMap<>();
			for (String term : words) {
				int freq = Collections.frequency(allWords, term);
				double IDF = Math.log((double) (numDocs) / freq);
				if (!retTerm2.containsKey(term))
					retTerm2.put(term, retTerm.get(term) * IDF);
			}
			ret.put(doc.getKey(), retTerm2);
		}
		return ret;
	}

	@Override
	public Map<?, ?> buildInvertedIndex(Map<String, Map<String, Double>> index) {

		Map<String, TreeSet<Entry<String, Double>>> ret = new HashMap<String, TreeSet<Entry<String, Double>>>();
		Comparator<Entry<String, Double>> comparebyTFIDF = new Comparator<Entry<String, Double>>() {

			@Override
			public int compare(Entry<String, Double> i1, Entry<String, Double> i2) {
				return i2.getValue().compareTo(i1.getValue());
			}
		};

		for (Map.Entry<String, Map<String, Double>> entry : index.entrySet()) {
			Map<String, Double> docTFIDF = entry.getValue();

			for (Map.Entry<String, Double> e : docTFIDF.entrySet()) {

				String term = e.getKey();
				Entry<String, Double> toAdd = new AbstractMap.SimpleEntry<String, Double>(entry.getKey(), e.getValue());

				if (ret.containsKey(term)) {
					ret.get(term).add(toAdd);
				} else {
					TreeSet<Entry<String, Double>> inverted = new TreeSet<Entry<String, Double>>(comparebyTFIDF);
					inverted.add(toAdd);
					ret.put(term, inverted);
				}

			}
		}

		return ret;
	}

	@Override
	public Collection<Entry<String, List<String>>> buildHomePage(Map<?, ?> invertedIndex) {

		Comparator<Entry<String, List<String>>> compareByArticlesThenByLex = new Comparator<Entry<String, List<String>>>() {
			@Override
			public int compare(Entry<String, List<String>> i1, Entry<String, List<String>> i2) {

				int diff = i2.getValue().size() - i1.getValue().size();

				if (diff == 0) {
					return i2.getKey().compareTo(i1.getKey());
				} else
					return diff;
			}
		};

		Collection<Entry<String, List<String>>> ret = new TreeSet<Entry<String, List<String>>>(
				compareByArticlesThenByLex);

		@SuppressWarnings("unchecked")
		Map<String, TreeSet<Entry<String, Double>>> index = (Map<String, TreeSet<Entry<String, Double>>>) invertedIndex;
		index.keySet().removeAll(STOPWORDS);

		for (Map.Entry<String, TreeSet<Entry<String, Double>>> entry : index.entrySet()) {
			String term = entry.getKey();
			TreeSet<Entry<String, Double>> list = entry.getValue();
			List<String> retList = new ArrayList<String>();
			for (Entry<String, Double> e : list) {
				retList.add(e.getKey());
			}
			Entry<String, List<String>> toAdd = new AbstractMap.SimpleEntry<String, List<String>>(term, retList);
			ret.add(toAdd);
		}

		return ret;
	}

	@Override
	public Collection<?> createAutocompleteFile(Collection<Entry<String, List<String>>> homepage) {

		List<String> words = new ArrayList<String>();

		for (Entry<String, List<String>> entry : homepage) {
			words.add(entry.getKey());
		}

		Collections.sort(words);
		words.remove("");

		try {
			BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream("autocomplete.txt"));
			stream.write((Integer.toString(words.size()) + "\n").getBytes());
			for (int i = 0; i < words.size(); i++) {
				String word = words.get(i);
				stream.write(("       0\t" + word + "\n").getBytes());
			}
			stream.close();
			return words;
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	@Override
	public List<String> searchArticles(String queryTerm, Map<?, ?> invertedIndex) {

		List<String> ret = new ArrayList<String>();

		@SuppressWarnings("unchecked")
		Map<String, TreeSet<Entry<String, Double>>> index = (Map<String, TreeSet<Entry<String, Double>>>) invertedIndex;
		TreeSet<Entry<String, Double>> articles = index.get(queryTerm);

		if (articles == null || articles.size() == 0) {
			return ret;
		}

		for (Entry<String, Double> e : articles) {
			ret.add(e.getKey());
		}

		return ret;
	}

}
