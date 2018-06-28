package de.hpi.unicorn.application.pages.input.generator.validation;

import de.hpi.unicorn.event.attribute.TypeTreeNode;
import org.apache.wicket.validation.IValidator;

import java.util.regex.Pattern;

/**
 * {@link IValidator}, you can use to get the actual Validator you need for web-app purposes.
 * Representation of the @de.hpi.unicorn.validation.AttributeValidator to be able to show error messages in frontend.
 */
public abstract class AttributeValidator implements IValidator<String> {

    /**
     * Get the fitting validator for the type of the given attribute.
     *
     * @param attribute the attribute of an event type the fitting validator should be determined for.
     * @return validator for given attribute
     */
    public static IValidator<String> getValidatorForAttribute(TypeTreeNode attribute) {
        switch (attribute.getType()) {
            case INTEGER:
                return new IntegerRangeValidator();
            case STRING:
                return new RegexValidator(Pattern.compile("\\w+(?:(?:\\s|\\-|\\,\\s\\:)\\w+)*(?:;\\w+(?:(?:\\s|\\-|\\,\\s\\:)\\w+)*)*"));
            case FLOAT:
                return new RegexValidator(Pattern.compile("\\d+(?:\\.\\d+)?(?:;\\d+(?:\\.\\d+)?)*"));
            case DATE:
                return new DateRangeValidator();
            default:
                return new RegexValidator(Pattern.compile(""));
        }
    }
}
