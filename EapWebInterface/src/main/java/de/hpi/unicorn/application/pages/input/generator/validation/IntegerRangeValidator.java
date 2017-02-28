package de.hpi.unicorn.application.pages.input.generator.validation;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import java.util.regex.Pattern;

/**
 * {@link IValidator}, that checks if input matches integer range pattern
 */
public class IntegerRangeValidator extends AttributeValidator {

    private static final String INTEGER_RANGE_PATTERN = "(?:\\d+(?:;\\d+)*|\\d+\\-\\d+)";
    private final Pattern pattern;

    /**
     * Constructor for the integer range validator.
     */
    public IntegerRangeValidator() {
        pattern = Pattern.compile(INTEGER_RANGE_PATTERN);
    }

    /**
     * Checks if given validatable satisfies the integer pattern
     *
     * @param validatable
     */
    @Override
    public void validate(IValidatable<String> validatable) {
        de.hpi.unicorn.validation.IntegerRangeValidator validator = new de.hpi.unicorn.validation.IntegerRangeValidator();
        boolean success = validator.validate(validatable.getValue());
        if (!success) {
            error(validatable, "noIntegerRange");
        }
    }

    /**
     * Throws error including the name of the field with wrong input.
     *
     * @param validatable
     * @param errorKey
     */
    private void error(IValidatable<String> validatable, String errorKey) {
        ValidationError error = new ValidationError();
        error.addKey(getClass().getSimpleName() + "." + errorKey);
        validatable.error(error);
    }
}