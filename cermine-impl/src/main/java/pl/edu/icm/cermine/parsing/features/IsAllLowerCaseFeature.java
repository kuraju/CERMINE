package pl.edu.icm.cermine.parsing.features;

import pl.edu.icm.cermine.parsing.model.ParsableString;
import pl.edu.icm.cermine.parsing.model.Token;
import pl.edu.icm.cermine.parsing.tools.TextClassifier;

/**
 * @author Bartosz Tarnawski
 *
 */
public class IsAllLowerCaseFeature extends BinaryTokenFeatureCalculator {

	@Override
	public boolean calculateFeaturePredicate(Token<?> token, ParsableString<?> context) {
		return TextClassifier.isAllLowerCase(token.getText());
	}

}
