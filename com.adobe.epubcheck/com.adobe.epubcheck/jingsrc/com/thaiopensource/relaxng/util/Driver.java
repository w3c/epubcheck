package com.thaiopensource.relaxng.util;

import com.thaiopensource.util.OptionParser;
import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.util.Version;
import com.thaiopensource.util.Localizer;
import com.thaiopensource.validate.Flag;
import com.thaiopensource.validate.Option;
import com.thaiopensource.validate.OptionArgumentException;
import com.thaiopensource.validate.SchemaReader;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.ValidationDriver;
import com.thaiopensource.validate.auto.AutoSchemaReader;
import com.thaiopensource.validate.rng.CompactSchemaReader;
import com.thaiopensource.validate.rng.RngProperty;
import com.thaiopensource.xml.sax.ErrorHandlerImpl;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;

class Driver {
  static private String usageKey = "usage";

  static public void setUsageKey(String key) {
    usageKey = key;
  }

  static public void main(String[] args) {
    System.exit(new Driver().doMain(args));
  }

  private boolean timing = false;
  private String encoding = null;
  private Localizer localizer = new Localizer(Driver.class);

  public int doMain(String[] args) {
    ErrorHandlerImpl eh = new ErrorHandlerImpl(System.out);
    OptionParser op = new OptionParser("itcdfe:p:", args);
    PropertyMapBuilder properties = new PropertyMapBuilder();
    ValidateProperty.ERROR_HANDLER.put(properties, eh);
    RngProperty.CHECK_ID_IDREF.add(properties);
    SchemaReader sr = null;
    boolean compact = false;

    try {
      while (op.moveToNextOption()) {
        switch (op.getOptionChar()) {
        case 'i':
          properties.put(RngProperty.CHECK_ID_IDREF, null);
          break;
        case 'c':
          compact = true;
          break;
        case 'd':
          {
            if (sr == null)
              sr = new AutoSchemaReader();
            Option option = sr.getOption(SchemaReader.BASE_URI + "diagnose");
            if (option == null) {
              eh.print(localizer.message("no_schematron", op.getOptionCharString()));
              return 2;
            }
            properties.put(option.getPropertyId(), Flag.PRESENT);
          }
          break;
        case 't':
          timing = true;
          break;
        case 'e':
          encoding = op.getOptionArg();
          break;
        case 'f':
          RngProperty.FEASIBLE.add(properties);
          break;
        case 'p':
          {
            if (sr == null)
              sr = new AutoSchemaReader();
            Option option = sr.getOption(SchemaReader.BASE_URI + "phase");
            if (option == null) {
              eh.print(localizer.message("no_schematron", op.getOptionCharString()));
              return 2;
            }
            try {
              properties.put(option.getPropertyId(), option.valueOf(op.getOptionArg()));
            }
            catch (OptionArgumentException e) {
              eh.print(localizer.message("invalid_phase", op.getOptionArg()));
              return 2;
            }
          }
          break;
        }
      }
    }
    catch (OptionParser.InvalidOptionException e) {
      eh.print(localizer.message("invalid_option", op.getOptionCharString()));
      return 2;
    }
    catch (OptionParser.MissingArgumentException e) {
      eh.print(localizer.message("option_missing_argument",op.getOptionCharString()));
      return 2;
    }
    if (compact)
      sr = CompactSchemaReader.getInstance();
    args = op.getRemainingArgs();
    if (args.length < 1) {
      eh.print(localizer.message(usageKey, Version.getVersion(Driver.class)));
      return 2;
    }
    long startTime = System.currentTimeMillis();
    long loadedPatternTime = -1;
    boolean hadError = false;
    try {
      ValidationDriver driver = new ValidationDriver(properties.toPropertyMap(), sr);
      InputSource in = ValidationDriver.uriOrFileInputSource(args[0]);
      if (encoding != null)
        in.setEncoding(encoding);
      if (driver.loadSchema(in)) {
        loadedPatternTime = System.currentTimeMillis();
	for (int i = 1; i < args.length; i++) {
	  if (!driver.validate(ValidationDriver.uriOrFileInputSource(args[i])))
	    hadError = true;
	}
      }
      else
	hadError = true;
    }
    catch (SAXException e) {
      hadError = true;
      eh.printException(e);
    }
    catch (IOException e) {
      hadError = true;
      eh.printException(e);
    }
    if (timing) {
      long endTime = System.currentTimeMillis();
      if (loadedPatternTime < 0)
        loadedPatternTime = endTime;
      eh.print(localizer.message("elapsed_time",
		       new Object[] {
                         new Long(loadedPatternTime - startTime),
                         new Long(endTime - loadedPatternTime),
                         new Long(endTime - startTime)
                       }));
    }
    if (hadError)
      return 1;
    return 0;
  }

}
