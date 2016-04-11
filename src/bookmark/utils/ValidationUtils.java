package bookmark.utils;

import java.util.regex.PatternSyntaxException;

import org.eclipse.jface.dialogs.IInputValidator;

public class ValidationUtils {

	public static IInputValidator getIInputValidatorInstance() {
		IInputValidator validator = new IInputValidator() {

			@Override
			public String isValid(String input) {
				try {
					if (input.matches("(?s)[^\\\\/:*?\"<>|\\x00-\\x1F]+$")) {
						return null;
					} else {
						return "Invalid file name";
					}
				} catch (PatternSyntaxException ex) {
					// Syntax error in the regular expression
				}
				return "There occure an error in the bookmark plugin,report it to the author.";
			}

		};
		return validator;
	}

}
