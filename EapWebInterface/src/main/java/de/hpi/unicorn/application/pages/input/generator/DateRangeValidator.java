package de.hpi.unicorn.application.pages.input.generator;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

/**
 * {@link IValidator}, that checks if input matches date range pattern.
 */
public class DateRangeValidator implements IValidator<String> {

    private static final String DATE = "(\\d{4}\\/\\d{2}\\/\\d{2}T\\d{2}\\:\\d{2})";
    private static final String DATE_RANGE_PATTERN = DATE + "||" + DATE + "-" + DATE;
    private final Pattern pattern;

    /**
     * Constructor for the date range validator.
     */
    DateRangeValidator() {
        pattern = Pattern.compile(DATE_RANGE_PATTERN);
    }

    /**
     * Checks if given validatable satisfies the date pattern.
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
            DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd'T'HH:mm");
            try {
                if (formatter.parse(splits[0]).after(formatter.parse(splits[1])))
                    error(validatable, "endBeforeStart");
            } catch (Exception e) {
                e.printStackTrace();
            }
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