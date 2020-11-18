package test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.SortedSet;

import org.junit.Test;

import indexing.IIndexBuilder;
import indexing.IndexBuilder;

/**
 * @author alonb
 */
public class TestIndexBuilder {
	/**
	 * Tests parseFeed for:
	 * map has the correct number of files 
	 * map contains the names of the documents (URLs/keys)
	 * map contains the correct number of terms in the lists (values)
	 * map contains the correct words
	 */
	@Test
	public void testParseFeed() {
		IIndexBuilder I = new IndexBuilder();
		List<String> feeds = new ArrayList<String>();
		feeds.add("http://cit594.ericfouh.com/sample_rss_feed.xml");
		Map<String, List<String>> test = I.parseFeed(feeds);
		assertEquals(5, test.size());
		assertTrue(test.containsKey("http://cit594.ericfouh.com/page1.html"));
		assertEquals(10, test.get("http://cit594.ericfouh.com/page1.html").size());
		assertTrue(test.containsKey("http://cit594.ericfouh.com/page2.html"));
		assertEquals(55, test.get("http://cit594.ericfouh.com/page2.html").size());
		assertTrue(test.containsKey("http://cit594.ericfouh.com/page3.html"));
		assertEquals(33, test.get("http://cit594.ericfouh.com/page3.html").size());
		assertTrue(test.containsKey("http://cit594.ericfouh.com/page4.html"));
		assertEquals(22, test.get("http://cit594.ericfouh.com/page4.html").size());
		assertTrue(test.containsKey("http://cit594.ericfouh.com/page5.html"));
		assertEquals(18, test.get("http://cit594.ericfouh.com/page5.html").size());
		assertEquals("data", test.get("http://cit594.ericfouh.com/page1.html").get(0));
		assertEquals("structures", test.get("http://cit594.ericfouh.com/page1.html").get(1));
		assertEquals("lets", test.get("http://cit594.ericfouh.com/page5.html").get(0));
		assertEquals("cit594", test.get("http://cit594.ericfouh.com/page5.html").get(11));
	}

	/**
	 * Tests buildIndex for:
	 * the TFIDF values from the test files
	 * value maps are sorted in lexicographic order by key
	 * map has the correct number of files
	 * has the correct number of words in the value map
	 */
	@Test
	public void testBuildIndex() {
		IIndexBuilder I = new IndexBuilder();
		List<String> feeds = new ArrayList<String>();
		feeds.add("http://cit594.ericfouh.com/sample_rss_feed.xml");
		Map<String, Map<String, Double>> index = I.buildIndex(I.parseFeed(feeds));
		assertEquals(5, index.size());
		Map<String, Double> p1 = index.get("http://cit594.ericfouh.com/page1.html");
		Map<String, Double> p2 = index.get("http://cit594.ericfouh.com/page2.html");
		Map<String, Double> p3 = index.get("http://cit594.ericfouh.com/page3.html");
		Map<String, Double> p4 = index.get("http://cit594.ericfouh.com/page4.html");
		Map<String, Double> p5 = index.get("http://cit594.ericfouh.com/page5.html");

		assertEquals(8, p1.size());

		assertEquals(0.1021, p1.get("data"), 0.001);
		assertEquals(0.183, p1.get("structures"), 0.001);
		assertEquals(0.0585, p2.get("search"), 0.001);
		assertEquals(0.0666, p2.get("structures"), 0.001);
		assertEquals(0.0277, p3.get("binary"), 0.001);
		assertEquals(0.04877, p3.get("implement"), 0.001);
		assertEquals(0.1463, p4.get("do"), 0.001);
		assertEquals(0.0509, p5.get("files"), 0.001);
		assertEquals(0.0894, p5.get("completely"), 0.001);

		List<Map.Entry<String, Double>> p1List = new ArrayList<Map.Entry<String, Double>>(p1.entrySet());
		assertEquals("arraylist", p1List.get(0).getKey());
		assertEquals("data", p1List.get(1).getKey());

		List<Map.Entry<String, Double>> p5List = new ArrayList<Map.Entry<String, Double>>(p5.entrySet());
		assertEquals("about", p5List.get(0).getKey());
		assertEquals("and", p5List.get(1).getKey());
	}

	/**
	 * Tests buildInvertedIndex for:
	 * map is of the correct type (of Map)
	 * map associates the correct files to a term
	 * map stores the documents in the correct order
	 */
	@Test
	public void testBuildInvertedIndex() {
		IIndexBuilder I = new IndexBuilder();
		List<String> feeds = new ArrayList<String>();
		feeds.add("http://cit594.ericfouh.com/sample_rss_feed.xml");
		Map<String, Map<String, Double>> index = I.buildIndex(I.parseFeed(feeds));
		Object invertedIndexO = I.buildInvertedIndex(index);

		assertTrue("the inverted index is not of the correct type", invertedIndexO instanceof Map);

		@SuppressWarnings("unchecked")
		Map<String, TreeSet<Entry<String, Double>>> invertedIndex = (Map<String, TreeSet<Entry<String, Double>>>) invertedIndexO;

		List<Entry<String, Double>> data = new ArrayList<Entry<String, Double>>(invertedIndex.get("data"));
		assertEquals("http://cit594.ericfouh.com/page1.html", data.get(0).getKey());

		List<Entry<String, Double>> and = new ArrayList<Entry<String, Double>>(invertedIndex.get("and"));
		assertEquals("http://cit594.ericfouh.com/page5.html", and.get(0).getKey());
		assertEquals("http://cit594.ericfouh.com/page3.html", and.get(1).getKey());
		assertEquals("http://cit594.ericfouh.com/page2.html", and.get(2).getKey());

	}

	/**
	 * Tests buildHomePage for:
	 * Collection is of the correct type
	 * collection stores the entries in the correct order
	 */
	@Test
	public void testBuildHomePage() {
		IIndexBuilder I = new IndexBuilder();
		List<String> feeds = new ArrayList<String>();
		feeds.add("http://cit594.ericfouh.com/sample_rss_feed.xml");
		Map<String, Map<String, Double>> index = I.buildIndex(I.parseFeed(feeds));

		@SuppressWarnings("unchecked")
		Map<String, TreeSet<Entry<String, Double>>> invertedIndex = (Map<String, TreeSet<Entry<String, Double>>>) I
				.buildInvertedIndex(index);

		Collection<Entry<String, List<String>>> homepage = I.buildHomePage(invertedIndex);

		assertTrue("the homepage is not of the correct type", homepage instanceof SortedSet);

		List<Entry<String, List<String>>> homepageList = new ArrayList<Entry<String, List<String>>>(homepage);

		assertEquals("data", homepageList.get(0).getKey());
		assertEquals("trees", homepageList.get(1).getKey());

	}

	/**
	 * Tests searchArticles for:
	 * list contains the correct number of articles
	 * list contains the correct articles
	 */
	@Test
	public void testSearchArticles() {
		IIndexBuilder I = new IndexBuilder();
		List<String> feeds = new ArrayList<String>();
		feeds.add("http://cit594.ericfouh.com/sample_rss_feed.xml");
		Map<String, Map<String, Double>> index = I.buildIndex(I.parseFeed(feeds));

		@SuppressWarnings("unchecked")
		Map<String, TreeSet<Entry<String, Double>>> invertedIndex = (Map<String, TreeSet<Entry<String, Double>>>) I
				.buildInvertedIndex(index);

		List<String> dataArticles = I.searchArticles("data", invertedIndex);
		assertEquals(3, dataArticles.size());
		assertEquals("http://cit594.ericfouh.com/page1.html", dataArticles.get(0));
		assertEquals("http://cit594.ericfouh.com/page2.html", dataArticles.get(1));
		assertEquals("http://cit594.ericfouh.com/page3.html", dataArticles.get(2));

		List<String> typeArticles = I.searchArticles("type", invertedIndex);
		assertEquals(1, typeArticles.size());
		assertEquals("http://cit594.ericfouh.com/page3.html", typeArticles.get(0));

	}

	/**
	 * test createAutocompleteFile for:
	 * collection is of the correct type
	 * collection contains the correct number of words
	 */
	@Test
	public void testCreateAutocompleteFile() {
		IIndexBuilder I = new IndexBuilder();
		List<String> feeds = new ArrayList<String>();
		feeds.add("http://cit594.ericfouh.com/sample_rss_feed.xml");
		Map<String, Map<String, Double>> index = I.buildIndex(I.parseFeed(feeds));

		@SuppressWarnings("unchecked")
		Map<String, TreeSet<Entry<String, Double>>> invertedIndex = (Map<String, TreeSet<Entry<String, Double>>>) I
				.buildInvertedIndex(index);

		Collection<Entry<String, List<String>>> homepage = I.buildHomePage(invertedIndex);

		Object autocompleteCollection = I.createAutocompleteFile(homepage);

		assertTrue("the autocomplete Collection is not of the correct type", autocompleteCollection instanceof List);

		@SuppressWarnings("unchecked")
		List<String> acWords = (List<String>) autocompleteCollection;

		assertEquals(57, acWords.size());

	}

}
