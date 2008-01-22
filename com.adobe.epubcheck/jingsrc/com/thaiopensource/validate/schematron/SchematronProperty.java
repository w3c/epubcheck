package com.thaiopensource.validate.schematron;

import com.thaiopensource.validate.StringPropertyId;
import com.thaiopensource.validate.FlagPropertyId;
import com.thaiopensource.validate.Option;
import com.thaiopensource.validate.FlagOption;
import com.thaiopensource.validate.StringOption;
import com.thaiopensource.validate.OptionArgumentFormatException;
import com.thaiopensource.validate.SchemaReader;
import com.thaiopensource.xml.util.Naming;

/**
 * Properties for controlling schema reading and validation specific to Schematron.
 */
public class SchematronProperty {
  private SchematronProperty() { }

  /**
   * PropertyId that specifies the Schematron phase to use.
   * This applies during schema creation.
   */
  public static final StringPropertyId PHASE = new StringPropertyId("PHASE");

  static public class PhaseOption extends StringOption {
    private PhaseOption() {
      super(PHASE);
    }

    public String normalize(String value) throws OptionArgumentFormatException {
      value = value.trim();
      if (!value.equals("#ALL") && !Naming.isNcname(value))
        throw new OptionArgumentFormatException();
      return value;
    }
  }

  public static final StringOption PHASE_OPTION = new PhaseOption();

  /**
   * PropertyId thats specifies that diagnostic messages should be included.
   * This applies during validation.
   */
  public static final FlagPropertyId DIAGNOSE = new FlagPropertyId("DIAGNOSE");

  public static Option getOption(String uri) {
    if (!uri.startsWith(SchemaReader.BASE_URI))
      return null;
    uri = uri.substring(SchemaReader.BASE_URI.length());
    if (uri.equals("diagnose"))
      return new FlagOption(DIAGNOSE);
    if (uri.equals("phase"))
      return PHASE_OPTION;
    return null;
  }
}
