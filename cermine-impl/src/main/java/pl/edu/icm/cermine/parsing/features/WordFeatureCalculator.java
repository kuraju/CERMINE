package pl.edu.icm.cermine.parsing.features;

import java.util.List;

import pl.edu.icm.cermine.parsing.model.Token;
import pl.edu.icm.cermine.parsing.model.TokenizedString;

/**
 * Class producing feature strings representing a word
 * 
 * @author Bartosz Tarnawski
 */
public class WordFeatureCalculator {

	private List<BinaryTokenFeatureCalculator> blockingFeatures;
	private boolean toLowerCase;
	// This is a GRMM convention, see: https://dl.dropboxusercontent.com/u/55174954/grmm.htm
	private static final String PREFIX = "W=";
	
	/**
	 * @param blockingFeatures the word feature will not be produced if a token has any of
	 * these features
	 * @param toLowerCase whether the word feature should be converted to lower case
	 */
	public WordFeatureCalculator(List<BinaryTokenFeatureCalculator> blockingFeatures,
			boolean toLowerCase) {
		this.blockingFeatures = blockingFeatures;
		this.toLowerCase = toLowerCase;
	}
	
    /**
     * @param token
     * @param context
     * @return the word represented by the token in an appropriate format or null if the token
     * has a blocking feature
     */
    public String calculateFeatureValue(Token<?> token, TokenizedString<?> context) {
		for (BinaryTokenFeatureCalculator feature : blockingFeatures) {
			if (feature.calculateFeaturePredicate(token, context)) {
				return null;
			}
		}
		if (toLowerCase) {
			return PREFIX + token.getText().toLowerCase(); 
		} else {
			return PREFIX + token.getText();
		}
    }
}