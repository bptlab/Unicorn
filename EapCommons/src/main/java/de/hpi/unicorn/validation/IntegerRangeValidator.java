package de.hpi.unicorn.validation;

import java.util.regex.Pattern;

/**
 * Validator, that checks if input matches integer range pattern.
 */
public class IntegerRangeValidator extends AttributeValidator {

    private static final String INTEGER_RANGE_PATTERN = "(?:\\d+(?:;\\d+)*|\\d+\\-\\d+)";
    private final Pattern pattern;

    public IntegerRangeValidator() {
        pattern = Pattern.compile(INTEGER_RANGE_PATTERN);
    }

    /**
     * Checks if given validatable satisfies the integer pattern.
     *
     * @param validatable
     */
    @Override
    public boolean validate(final String validatable) {
        //get input from attached component
        final String input = validatable;
        if (!pattern.matcher(input).matches()) {
            return false;
        }
        String[] splits;
        if (input.contains("-")) {
            splits = input.split("-");
            if(Integer.parseInt(splits[0]) > Integer.parseInt(splits[1]))
                return false;
        }
        return true;
    }
}