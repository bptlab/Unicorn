package de.hpi.unicorn.application.pages.input.generator.validation;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import java.util.regex.Pattern;

/**
 * {@link IValidator}, that checks if input matches date range pattern.
 */
public class DateRangeValidator extends AttributeValidator {

    private static final String DATE = "(\\d{4}\\/\\d{2}\\/\\d{2}T\\d{2}\\:\\d{2})";
    private static final String DATE_RANGE_PATTERN = DATE + "||" + DATE + "-" + DATE;
    private final Pattern pattern;
    /**
     * Constructor for the date range validator.
     */
    public DateRangeValidator() {
        pattern = Pattern.compile(DATE_RANGE_PATTERN);
    }

    /**
     * Checks if given validatable satisfies the date pattern.
     *
     * @param validatable the object to be validated
     */
    @Override
    public void validate(IValidatable<String> validatable) {
        de.hpi.unicorn.validation.DateRangeValidator validator = new de.hpi.unicorn.validation.DateRangeValidator();
        boolean success = validator.validate(validatable.getValue());
        if (!success) {
            error(validatable, "noDateRange");
        }
    }

    /**
     * Throws error including the name of the field with wrong input.
     *
     * @param validatable the object to be validated
     * @param errorKey a string to be concatinated to the error
     */
    private void error(IValidatable<String> validatable, String errorKey) {
        ValidationError error = new ValidationError();
        error.addKey(getClass().getSimpleName() + "." + errorKey);
        validatable.error(error);
    }
}
