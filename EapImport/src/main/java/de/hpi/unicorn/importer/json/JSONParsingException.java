package de.hpi.unicorn.importer.json;

/**
 * A exception for error while parsing XML files.
 *
 * @author mlichtblau
 */
@SuppressWarnings("serial")
public class JSONParsingException extends Exception {

    public JSONParsingException() {

    }

    public JSONParsingException(final String s) { super(s); }
}
