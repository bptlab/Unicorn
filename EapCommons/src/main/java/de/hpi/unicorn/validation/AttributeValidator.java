package de.hpi.unicorn.validation;

import de.hpi.unicorn.event.attribute.TypeTreeNode;
import java.util.regex.Pattern;


/**
 * Abstract validator you can use to get the actual Validator you need.
 * In case you need a wicket IValidator, use @AttributeValidator in
 * de.hpi.unicorn.application.pages.input.generator.validation instead.
 */
public abstract class AttributeValidator {

    /**
     * Get the fitting validator for the type of the given attribute.
     *
     * @param attribute
     * @return validator for given attribute
     */
    public static AttributeValidator getValidatorForAttribute(TypeTreeNode attribute) {
        switch (attribute.getType()) {
            case INTEGER:
                return new IntegerRangeValidator();
            case STRING:
                return new RegexValidator(Pattern.compile("\\w+(?:(?:\\s|\\-|\\,\\s)\\w+)*(?:;\\w+(?:(?:\\s|\\-|\\,\\s)\\w+)*)*"));
            case FLOAT:
                return new RegexValidator(Pattern.compile("\\d+(?:\\.\\d+)?(?:;\\d+(?:\\.\\d+)?)*"));
            case DATE:
                return new DateRangeValidator();
            default:
                return new RegexValidator(Pattern.compile(""));
        }
    }

    public abstract boolean validate(String validatable);
}