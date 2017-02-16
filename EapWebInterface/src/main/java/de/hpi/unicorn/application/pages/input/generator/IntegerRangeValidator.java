package de.hpi.unicorn.application.pages.input.generator;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import java.util.regex.Pattern;

/**
 * {@link IValidator}, that checks if input matches integer range pattern
 */
public class IntegerRangeValidator implements IValidator<String> {

    private static final String INTEGER_RANGE_PATTERN = "(?:\\d+(?:;\\d+)*|\\d+\\-\\d+)";
    private final Pattern pattern;

    IntegerRangeValidator() {
        pattern = Pattern.compile(INTEGER_RANGE_PATTERN);
    }

    /**
     * Checks if given validatable satisfies the integer pattern
     *
     * @param validatable
     */
    @Override
    public void validate(IValidatable<String> validatable) {
        //get input from attached component
        final String input = validatable.getValue();
        if (!pattern.matcher(input).matches()) {
            error(validatable,"noIntegerRange");
            return;
        }
        String[] splits;
        if (input.contains("-")) {
            splits = input.split("-");
            if(Integer.parseInt(splits[0]) > Integer.parseInt(splits[1]))
                error(validatable, "endBeforeStart");
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