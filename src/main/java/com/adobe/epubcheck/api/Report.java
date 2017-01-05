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

import com.adobe.epubcheck.messages.Message;
import com.adobe.epubcheck.messages.MessageDictionary;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.util.FeatureEnum;

import java.io.File;

/**
 * Interface that is used to report issues found in epub.
 */
public interface Report
{
  /**
   * Called when a violation of the standard is found in epub.
   *
   * @param id       Id of the message being reported
   * @param location location information for the message
   * @param args     Arguments referenced by the format
   *                 string for the message.
   */
  public void message(MessageId id, EPUBLocation location, Object... args);

  /**
   * Called when a violation of the standard is found in epub.
   *
   * @param message  The message being reported
   * @param location location information for the message
   * @param args     Arguments referenced by the format
   *                 string for the message.
   */
  void message(Message message, EPUBLocation location, Object... args);

  /**
   * Called when when a feature is found in epub.
   *
   * @param resource name of the resource in the epub zip container that has this feature
   *                 or null if the feature is on the container level.
   * @param feature  a keyword to know what kind of feature has been found
   * @param value    value found
   */
  public void info(String resource, FeatureEnum feature, String value);

  public int getErrorCount();

  public int getWarningCount();

  public int getFatalErrorCount();

  public int getInfoCount();

  public int getUsageCount();

  /**
   * Called to create a report after the checks have been made
   */
  public int generate();

  /**
   * Called when a report if first created
   */
  public void initialize();

  public void setEpubFileName(String value);

  public String getEpubFileName();

  void setCustomMessageFile(String customMessageFileName);

  String getCustomMessageFile();

  public int getReportingLevel();

  public void setReportingLevel(int level);

  void close();

  void setOverrideFile(File customMessageFile);

  MessageDictionary getDictionary();
}
