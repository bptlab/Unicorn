package de.hpi.unicorn.configuration;

/**
 * Handling of multiple values in XML events
 * 
 * CROSS = output multiple events if attribute has multiple values (cross
 * product, each events has one of the values)
 * 
 * FIRST = output single event with first of multiple values for the affected
 * attribute
 * 
 * LAST = output single event with last of multiple values for the affected
 * attribute
 * 
 * CONCAT,[FIRST|LAST] = output single event, concatenate multiple values (only
 * applicable if attribute is specified as String, otherwise [FIRST|LAST]
 * applies or DEFAULT if neither of them is given)
 * 
 * @author tw
 */
public enum MultipleEventValueHandling {
	CROSS, FIRST, LAST, CONCAT
}
