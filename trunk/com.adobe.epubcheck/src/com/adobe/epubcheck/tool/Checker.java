/*
 * Copyright (c) 2007 Adobe Systems Incorporated
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy of
 *  this software and associated documentation files (the "Software"), to deal in
 *  the Software without restriction, including without limitation the rights to
 *  use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 *  the Software, and to permit persons to whom the Software is furnished to do so,
 *  subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 *  FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 *  COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 *  IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *  CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 *    <AdobeIP#0000474>
 */

package com.adobe.epubcheck.tool;

import java.io.File;

import com.adobe.epubcheck.api.EpubCheck;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.util.DefaultReportImpl;

public class Checker {

	public static void main(String[] args) {
		/*if (args.length != 1) {
			System.err.println("single argument expected");
			return;
		}*/
		//String epubName = args[0];
		String epubName = processArguments(args);
		
		Report report = new DefaultReportImpl(epubName);

		if (!epubName.endsWith(".epub"))
			report.warning(null, 0, "filename does not include '.epub' suffix");

		EpubCheck check = new EpubCheck(new File(epubName), report);
		if( check.validate() )
			System.out.println("No errors or warnings detected");
		else {
			System.err.println("\nCheck finished with warnings or errors!\n");
			System.exit(1); // Exit with status code 1 if there were errors reported
		}
	}
	
	/**
	 * This method iterates through all of the arguments passed to
	 * main to find accepted flags and the name of the file to check.
	 * This method returns the last argument that ends with ".epub"
	 * (which is assumed to be the file to check)
	 * Here are the currently accepted flags:
	 * <br>
	 * <br>-? or -help = display usage instructions
	 * <br>-v or -version = display tool version number
	 * 
	 * @param args String[] containing arguments passed to main
	 * @return the name of the file to check
	 */
	public static String processArguments(String[] args) {
		// Exit if there are no arguments passed to main
		displayVersion();
		if(args.length < 1) {
			System.err.println("At least one argument expected");
			System.exit(1);
		}
		
		String epubName = new String("");
		
		// For each element of args[], check to see if it is an accepted flag
		// or if it ends in ".epub" (and is therefore the file to check)
		for(int i = 0; i < args.length; i++) {
			if(args[i].equals("-version") || args[i].equals("-v"))
				displayVersion();
			if(args[i].equals("-help") || args[i].equals("-?"))
				displayHelp(); // display help message
			if(args[i].length() > 5)
				if(args[i].substring(args[i].length() - 5).equals(".epub"))
					epubName = args[i];
		}
		
		// If an argument ended in ".epub", return it
		if(!epubName.equals(""))
			return epubName;
		// Else no epub file to check was specified, so exit
		else {
			System.err.println("No .epub file to check was specified in arguments!");
			System.err.println("The tool will EXIT!");
			System.exit(1);
		}
		return null;
	}
	
	/**
	 * This method displays a short help message that describes the
	 * command-line usage of this tool
	 */
	public static void displayHelp() {
		displayVersion();
		System.out.println("When running this tool, the first argument " +
				"should be the name (and path) of the file to check.");
		System.out.println("This tool also accepts the following flags:");
		System.out.println("-? or -help 	= displays this help message");
		System.out.println("-v or -version 	= displays the tool's version number\n");
	}
	
	public static void displayVersion() {
		System.out.println("Epubcheck Version " + EpubCheck.VERSION+"\n");
	}
}
