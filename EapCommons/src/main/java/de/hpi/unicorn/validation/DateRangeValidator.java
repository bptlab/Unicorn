package de.hpi.unicorn.validation;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

/**
 * Validator that checks if input matches date range pattern.
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
     * @param validatable
     */
    @Override
    public boolean validate(String validatable) {
        //get input from attached component
        final String input = validatable;
        if (!pattern.matcher(input).matches()) {
            return false;
        }
        String[] splits;
        if (input.contains("-")) {
            splits = input.split("-");
            DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd'T'HH:mm");
            try {
                if (formatter.parse(splits[0]).after(formatter.parse(splits[1])))
                    return false;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }
}