package com.thaiopensource.validate;

/**
 * Thrown to indicate an XML document is not a correct schema, either because the
 * XML document is not well-formed or because it fails to be correct in some other
 * way.
 *
 * @see SchemaReader#createSchema
 *
 * @author <a href="mailto:jjc@jclark.com">James Clark</a>
 */
public class IncorrectSchemaException extends Exception {
}
