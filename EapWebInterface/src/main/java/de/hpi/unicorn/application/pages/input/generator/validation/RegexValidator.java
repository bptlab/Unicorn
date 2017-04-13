package de.hpi.unicorn.application.pages.input.generator.validation;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import java.util.regex.Pattern;

/**
 * {@link IValidator}, that checks if input matches the given pattern.
 */
public class RegexValidator extends AttributeValidator {

    Pattern pattern;
    /**
     * Constructor for the regex validator.
     * @param regex the pattern to be used for validation
     */
    public RegexValidator(Pattern regex) {
        pattern = regex;
    }

    /**
     * Checks if given validatable satisfies the pattern.
     *
     * @param validatable the object to be validated
     */
    @Override
    public void validate(IValidatable<String> validatable) {
        de.hpi.unicorn.validation.RegexValidator validator = new de.hpi.unicorn.validation.RegexValidator(pattern);
        boolean success = validator.validate(validatable.getValue());
        if (!success) {
            error(validatable);
        }
    }

    /**
     * Throws error including the name of the field with wrong input and the pattern.
     *
     * @param validatable the object to be validated
     */
    private void error(IValidatable<String> validatable) {
        ValidationError error = new ValidationError();
        error.setVariable("pattern", this.pattern.pattern());
        error.addKey(getClass().getSimpleName());
        validatable.error(error);
    }
}
