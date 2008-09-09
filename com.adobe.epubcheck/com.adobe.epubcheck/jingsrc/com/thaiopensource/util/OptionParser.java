package com.thaiopensource.util;

public class OptionParser {
  private final String optionSpec;
  private char optionChar = 0;
  private String optionArg = null;
  private int argIndex = 0;
  private int currentOptionIndex = 0;
  private final String[] args;

  private static final char OPTION_CHAR = '-';

  public static class MissingArgumentException extends Exception { }

  public static class InvalidOptionException extends Exception { }

  public OptionParser(String optionSpec, String[] args) {
    this.optionSpec = optionSpec;
    this.args = new String[args.length];
    System.arraycopy(args, 0, this.args, 0, args.length);
  }

  public char getOptionChar() {
    return optionChar;
  }

  public String getOptionCharString() {
    return new String(new char[]{optionChar});
  }

  public String getOptionArg() {
    return optionArg;
  }

  public boolean moveToNextOption()
    throws InvalidOptionException, MissingArgumentException {
    if (currentOptionIndex > 0
	&& currentOptionIndex == args[argIndex].length()) {
      currentOptionIndex = 0;
      argIndex++;
    }
    if (currentOptionIndex == 0) {
      if (argIndex >= args.length)
	return false;
      String arg = args[argIndex];
      if (arg.length() < 2 || arg.charAt(0) != OPTION_CHAR)
	return false;
      if (arg.length() == 2 && arg.charAt(1) == OPTION_CHAR) {
	argIndex++;
	return false;
      }
      currentOptionIndex = 1;
    }
    optionChar = args[argIndex].charAt(currentOptionIndex++);
    optionArg = null;
    int i = optionSpec.indexOf(optionChar);
    if (i < 0 || (optionChar == ':' && i > 0))
      throw new InvalidOptionException();
    if (i + 1 < optionSpec.length() && optionSpec.charAt(i + 1) == ':') {
      if (currentOptionIndex < args[argIndex].length()) {
	optionArg = args[argIndex].substring(currentOptionIndex);
	currentOptionIndex = 0;
	argIndex++;
      }
      else if (argIndex + 1 < args.length) {
	optionArg = args[++argIndex];
	++argIndex;
	currentOptionIndex = 0;
      }
      else
	throw new MissingArgumentException();
    }
    return true;
  }

  public String[] getRemainingArgs() {
    String[] tem = new String[args.length - argIndex];
    System.arraycopy(args, argIndex, tem, 0, tem.length);
    return tem;
  }

  public static void main(String[] args) {
    String optSpec = args[0];
    String[] tem = new String[args.length - 1];
    System.arraycopy(args, 1, tem, 0, tem.length);
    args = tem;
    OptionParser opts = new OptionParser(optSpec, args);
    try {
      while (opts.moveToNextOption()) {
	System.err.print("option " + opts.getOptionChar());
	String arg = opts.getOptionArg();
	if (arg == null)
	  System.err.println(" (no argument)");
	else
	  System.err.println(" arg=" + arg);
      }
      args = opts.getRemainingArgs();
      for (int i = 0; i < args.length; i++)
	System.err.println("arg=" + args[i]);
    }
    catch (OptionParser.MissingArgumentException e) {
      System.err.println("missing argument for option " + opts.getOptionChar());
    }
    catch (OptionParser.InvalidOptionException e) {
      System.err.println("invalid option " + opts.getOptionChar());
    }
  }
}
