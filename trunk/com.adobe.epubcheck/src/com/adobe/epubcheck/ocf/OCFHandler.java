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

package com.adobe.epubcheck.ocf;

import java.util.HashSet;

import com.adobe.epubcheck.xml.XMLElement;
import com.adobe.epubcheck.xml.XMLHandler;
import com.adobe.epubcheck.xml.XMLParser;

public class OCFHandler implements XMLHandler {

        XMLParser parser;

        HashSet containerEntries;

        static String rootPath;

        OCFHandler(XMLParser parser) {
                this.parser = parser;
                this.containerEntries = new HashSet();
        }

        public String getRootPath() {
                return rootPath;
        }

        public HashSet getContainerEntries() {
                return containerEntries;
        }

        public void startElement() {
                XMLElement e = parser.getCurrentElement();
                String ns = e.getNamespace();
                if (e.getName().equals("rootfile") && ns != null
                                && ns.equals("urn:oasis:names:tc:opendocument:xmlns:container")) {
                        String mediaType = e.getAttribute("media-type");
                        String fullPath = e.getAttribute("full-path");
                        containerEntries.add(fullPath);

                        if (mediaType != null
                                        && mediaType.equals("application/oebps-package+xml")) {
                                rootPath = fullPath;
                        }
                }
        }

        public void endElement() {
        }

        public void ignorableWhitespace(char[] chars, int arg1, int arg2) {
        }

        public void characters(char[] chars, int arg1, int arg2) {
        }

        public void processingInstruction(String arg0, String arg1) {
        }

}
