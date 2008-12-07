/**
 * 
 */
package com.adobe.epubcheck.autotest;

/**
 * @author Paul Norton
 *
 */
public class Result {

	private boolean valid = true;
	
	private int errors;
	
	private int warnings;

	public Result() {
		this.errors=0;
		this.warnings=0;
	}

	/**
	 * @param errors the errors to set
	 */
	public void setErrors(int errors) {
		this.errors = errors;
	}

	/**
	 * @return the errors
	 */
	public int getErrors() {
		return errors;
	}

	/**
	 * @param warnings the warnings to set
	 */
	public void setWarnings(int warnings) {
		this.warnings = warnings;
	}

	/**
	 * @return the warnings
	 */
	public int getWarnings() {
		return warnings;
	}

	/**
	 * @param valid the valid to set
	 */
	public void setValid(boolean valid) {
		this.valid = valid;
	}

	/**
	 * @return the valid
	 */
	public boolean isValid() {
		return valid;
	}
	
}
