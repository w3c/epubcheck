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
 */
package com.adobe.epubcheck.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.zip.ZipFile;

import com.adobe.epubcheck.ocf.OCFChecker;
import com.adobe.epubcheck.ocf.OCFPackage;
import com.adobe.epubcheck.ocf.OCFZipPackage;
import com.adobe.epubcheck.opf.DocumentValidator;
import com.adobe.epubcheck.util.CheckUtil;
import com.adobe.epubcheck.util.DefaultReportImpl;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.Messages;
import com.adobe.epubcheck.util.ResourceUtil;
import com.adobe.epubcheck.util.WriterReportImpl;

/**
 * Public interface to epub validator.
 */
public class EpubCheck implements DocumentValidator {
	
	private static String VERSION =null; 
	public static String version() {
		if (VERSION==null) {
			Properties prop = new Properties();
			InputStream in = EpubCheck.class.getResourceAsStream("project.properties");
			try {
				prop.load(in);
			} catch (Exception e) {
				System.out.println("Couldn't read project properties");
			} finally {
				if (in!=null){
					try {
						in.close();
					} catch (IOException e) {}
				}
			}	
			VERSION = prop.getProperty("version");
		}
		return VERSION;
	}
	
	private File epubFile;
	private Report report;
	private EPUBVersion version;

	/*
	 * Create an epub validator to validate the given file. Issues will be
	 * reported to standard error.
	 */
	public EpubCheck(File epubFile) {
		this(epubFile, new DefaultReportImpl(epubFile.getName()));
	}

	/*
	 * Create an epub validator to validate the given file. Issues will be
	 * reported to the given PrintWriter.
	 */
	public EpubCheck(File epubFile, PrintWriter out) {
		this(epubFile, new WriterReportImpl(out));
	}

	/*
	 * Create an epub validator to validate the given file and report issues to
	 * a given Report object.
	 */
	public EpubCheck(File epubFile, Report report) {
		this(epubFile, report, null);
	}

	public EpubCheck(File epubFile, Report report, EPUBVersion version) {
		this.epubFile = epubFile;
		this.report = report;
		this.version = version;
	}

	public EpubCheck(InputStream inputStream, Report report, String uri) {
		this(inputStream, report, uri, null);
	}

	public EpubCheck(InputStream inputStream, Report report, String uri,
			EPUBVersion version) {
		File epubFile;
		OutputStream out = null;
		try {
			epubFile = File.createTempFile("epub." + ResourceUtil.getExtension(uri), null);
			epubFile.deleteOnExit();
			out = new FileOutputStream(epubFile);

			byte[] bytes = new byte[1024];
			int read;
			while ((read = inputStream.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
			
			this.epubFile = epubFile;
			this.report = report;
			this.version = version;

		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			try{
				inputStream.close();
				out.flush();
				out.close();
			}catch (Exception e) {
				
			}
		}
	}

	/**
	 * Validate the file. Return true if no errors or warnings found.
	 */
	public boolean validate() {
		ZipFile zip = null;
		FileInputStream epubIn = null;
		try {		
			String extension = ResourceUtil.getExtension(epubFile.getName());
			if(extension != null) {
				if(!extension.equals("epub")) {
					if(extension.matches("[Ee][Pp][Uu][Bb]")){
						report.warning(epubFile.getName(), -1, -1, 
							"Use only lowercase characters for the EPUB file extension for maximum compatibility");
					} else {
						report.warning(epubFile.getName(), -1, -1, 
							"Uncommon EPUB file extension'" + extension + "'. For maximum compatibility, use '.epub'");
					}
				}
			}
			
			epubIn = new FileInputStream(epubFile);

			byte[] header = new byte[58];

			int readCount = epubIn.read(header);
			if (readCount != -1) {
				while (readCount < header.length) {
					int read = epubIn.read(header, readCount, header.length
							- readCount);
					// break on eof
					if (read == -1)
						break;
					readCount += read;
				}
			}
			if (readCount != header.length) {
				report.error(null, 0, 0, Messages.CANNOT_READ_HEADER);
			} else {
				int fnsize = getIntFromBytes(header, 26);
				int extsize = getIntFromBytes(header, 28);

				if (header[0] != 'P' && header[1] != 'K') {
					report.error(null, 0, 0, Messages.CORRUPTED_ZIP_HEADER);
				} else if (fnsize != 8) {
					report.error(null, 0, 0, String.format(
							Messages.LENGTH_FIRST_FILENAME, fnsize));
				} else if (extsize != 0) {
					report.error(null, 0, 0,
							String.format(Messages.EXTRA_FIELD_LENGTH, extsize));
				} else if (!CheckUtil.checkString(header, 30, "mimetype")) {
					report.error(null, 0, 0, Messages.MIMETYPE_ENTRY_MISSING);
				} else if (!CheckUtil.checkString(header, 38,
						"application/epub+zip")) {
					report.error(null, 0, 0, String.format(
							Messages.MIMETYPE_WRONG_TYPE,
							"application/epub+zip"));
				}
			}
		
			zip = new ZipFile(epubFile);

			OCFPackage ocf = new OCFZipPackage(zip);

			OCFChecker checker = new OCFChecker(ocf, report, version);

			checker.runChecks();
			
		} catch (IOException e) {
			report.error(null, 0, 0,
					String.format(Messages.IO_ERROR, e.getMessage()));
		} finally {
			try{
				epubIn.close();
				zip.close();
			}catch (Exception e) {
				
			}
		}
		return report.getWarningCount() == 0 && report.getErrorCount() == 0;
	}

	private int getIntFromBytes(byte[] bytes, int offset) {
		int hi = 0xFF & bytes[offset + 1];
		int lo = 0xFF & bytes[offset + 0];
		return hi << 8 | lo;
	}
	
}
