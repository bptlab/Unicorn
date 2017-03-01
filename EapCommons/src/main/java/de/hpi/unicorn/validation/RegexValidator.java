package de.hpi.unicorn.validation;

import java.util.regex.Pattern;

/**
 * Validator, that checks if input matches given pattern.
 */
public class RegexValidator extends AttributeValidator{

    private final Pattern regex;

    /**
     * Constructor for the regex validator.
     */
    public RegexValidator(Pattern regex) {
        this.regex = regex;
    }

    /**
     * Checks if given validatable satisfies the pattern.
     *
     * @param validatable
     */
    @Override
    public boolean validate(final String validatable) {
        if (!regex.matcher(validatable).matches()) {
            return false;
        }
        return true;
    }
}
