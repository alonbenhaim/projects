package autocomplete;


/**
 * @author alonb
 *
 */
public class Term implements ITerm {

	private String query;
	private long weight;

	/**
	 * Initializes a term with the given query string and weight
	 * 
	 * @param query  the word
	 * @param weight the weight of the word
	 * @throws IllegalArgumentException
	 */
	public Term(String query, long weight) throws IllegalArgumentException {
		if (query == null || weight < 0)
			throw new IllegalArgumentException();
		this.query = query;
		this.weight = weight;
	}

	@Override
	public int compareTo(ITerm that) {
		return this.query.compareTo(((Term) that).getTerm());
	}

	@Override
	public String toString() {
		return (this.weight + "\t" + this.query);
	}

	/**
	 * @return the query
	 */
	public String getTerm() {
		return this.query;
	}

	/**
	 * @param query the query to set
	 */
	public void setTerm(String query) {
		this.query = query;
	}

	/**
	 * @return the weight
	 */
	public long getWeight() {
		return this.weight;
	}

	/**
	 * @param weight the weight to set
	 */
	public void setWeight(long weight) {
		this.weight = weight;
	}

}
