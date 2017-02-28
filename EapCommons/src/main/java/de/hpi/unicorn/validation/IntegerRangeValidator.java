package de.hpi.unicorn.validation;

import org.apache.log4j.Logger;

import java.util.regex.Pattern;

/**
 * Validator, that checks if input matches integer range pattern.
 */
public class IntegerRangeValidator extends AttributeValidator {

    private static final String INTEGER_RANGE_PATTERN = "(?:\\d+(?:;\\d+)*|\\d+\\-\\d+)";
    private final Pattern pattern;
    private static Logger logger = Logger.getLogger(IntegerRangeValidator.class);

    public IntegerRangeValidator() {
        pattern = Pattern.compile(INTEGER_RANGE_PATTERN);
    }

    /**
     * Checks if given validatable satisfies the integer pattern.
     *
     * @param input
     */
    @Override
    public boolean validate(final String input) {
        if (!pattern.matcher(input).matches()) {
            logger.info("Input " + input + " doesnt match pattern: " + pattern.toString());
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