package pl.edu.icm.cermine.parsing.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;

/**
 * Representation of a token, an atomic part of a string. Used for string parsing.
 * 
 * @author Bartosz Tarnawski
 * @param <L> type of a label used for token type prediction
 */
public abstract class Token<L> {

	protected String text;
	protected int startIndex;
	protected int endIndex;
	protected L label;
	protected List<String> features;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
	public int getStartIndex() {
		return startIndex;
	}
	
	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}

	public int getEndIndex() {
		return endIndex;
	}

	public void setEndIndex(int endIndex) {
		this.endIndex = endIndex;
	}

	public L getLabel() {
		return label;
	}
	
	/**
	 * @return a valid GRMM label representing the token's type
	 */
	public abstract String getGrmmLabelString();

	/**
	 * @return a valid XML tag representing the token's type
	 */
	public abstract String getXmlTagString();
	
	public void setLabel(L label) {
		this.label = label;
	}
	
	public List<String> getFeatures() {
		return features;
	}

	public void setFeatures(List<String> features) {
		this.features = features;
	}
	
    public void addFeature(String feature) {
    	features.add(feature);
    }
	
	public Token(String text, int startIndex, int endIndex, L label) {
		this.text = text;
		this.startIndex = startIndex;
		this.endIndex = endIndex;
		this.label = label;
		this.features = new ArrayList<String>();
	}
	
	public Token(String text, int startIndex, int endIndex) {
		this(text, startIndex, endIndex, null);
	}

	public Token(String text) {
		this(text, 0, 0);
	}
	
	public Token() {
		this("");
	}
	
	// Ignores the label, for testing purposes only
	public boolean equals(Object obj) {
       if (!(obj instanceof Token))
            return false;
        if (obj == this)
            return true;

        @SuppressWarnings("rawtypes")
        Token rhs = (Token) obj;
        return new EqualsBuilder().
            append(text, rhs.text).
            append(startIndex, rhs.startIndex).
            append(endIndex, rhs.endIndex).
            isEquals();
    }
	
	/**
	 * Compares text strings represented by sequences of tokens
	 * 
	 * @param lhs
	 * @param rhs
	 * @param caseSensitive
	 * @return whether the corresponding strings are equal
	 */
	@SuppressWarnings("rawtypes")
	public static <T extends Token> boolean sequenceTextEquals(List<T> lhs, List<T> rhs,
			boolean caseSensitive) {
		if (lhs.size() != rhs.size()) {
			return false;
		}
		
		for (int i = 0; i < lhs.size(); i++) {
			String lhsString = lhs.get(i).getText();
			String rhsString = rhs.get(i).getText();
			if (!caseSensitive) {
				lhsString = lhsString.toLowerCase();
				rhsString = rhsString.toLowerCase();
			}
			if (!lhsString.equals(rhsString)) {
				return false;
			}
		}
		
		return true;
	}
}