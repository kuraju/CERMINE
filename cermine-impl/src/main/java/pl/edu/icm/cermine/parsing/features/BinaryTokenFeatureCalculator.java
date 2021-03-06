package pl.edu.icm.cermine.parsing.features;

import pl.edu.icm.cermine.parsing.model.ParsableString;
import pl.edu.icm.cermine.parsing.model.Token;
import pl.edu.icm.cermine.tools.classification.features.FeatureCalculator;

/**
 * Feature calculator which checks whether a token (representing a word) has a given feature.
 * 
 * @author Bartosz Tarnawski
 */
public abstract class BinaryTokenFeatureCalculator extends
FeatureCalculator<Token<?>, ParsableString<?>> {

	/**
	 * @param token
	 * @param context
	 * @return whether the token in the context has the feature represented by the class
	 */
	public abstract boolean calculateFeaturePredicate(Token<?> token, ParsableString<?> context);

	@Override
    public double calculateFeatureValue(Token<?> token, ParsableString<?> context) {
    	if (calculateFeaturePredicate(token, context)) {
    		return 1;
    	} else {
    		return 0;
    	}
    }
}
