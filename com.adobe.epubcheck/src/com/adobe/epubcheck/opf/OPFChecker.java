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

package com.adobe.epubcheck.opf;

import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.bitmap.BitmapCheckerFactory;
import com.adobe.epubcheck.dtbook.DTBookCheckerFactory;
import com.adobe.epubcheck.ncx.NCXCheckerFactory;
import com.adobe.epubcheck.ocf.OCFPackage;
import com.adobe.epubcheck.ops.OPSCheckerFactory;
import com.adobe.epubcheck.xml.XMLParser;
import com.adobe.epubcheck.xml.XMLValidator;

public class OPFChecker {

        OCFPackage ocf;

        Report report;

        String path;

        HashSet containerEntries;

        static XMLValidator opfValidator = new XMLValidator("rng/opf.rng");

        static XMLValidator opfSchematronValidator = new XMLValidator("sch/opf.sch");

        XRefChecker xrefChecker;

        static Hashtable contentCheckerFactoryMap;

        static {
                Hashtable map = new Hashtable();
                map.put("application/xhtml+xml", OPSCheckerFactory.getInstance());
                map.put("text/html", OPSCheckerFactory.getInstance());
                map.put("text/x-oeb1-document", OPSCheckerFactory.getInstance());
                map.put("image/jpeg", BitmapCheckerFactory.getInstance());
                map.put("image/gif", BitmapCheckerFactory.getInstance());
                map.put("image/png", BitmapCheckerFactory.getInstance());
                map.put("image/svg+xml", OPSCheckerFactory.getInstance());
                map.put("application/x-dtbook+xml", DTBookCheckerFactory.getInstance());

                contentCheckerFactoryMap = map;
        }

        public OPFChecker(OCFPackage ocf, Report report, String path, HashSet containerEntries) {
                this.ocf = ocf;
                this.report = report;
                this.path = path;
                this.containerEntries = containerEntries;
                this.xrefChecker = new XRefChecker(ocf, report);
        }

        public void runChecks() {
                if (!ocf.hasEntry(path))
                        report.error(null, 0, "OPF file " + path + " is missing");
                else {
                        XMLParser opfParser = new XMLParser(ocf, path, report);
                        OPFHandler opfHandler = new OPFHandler(opfParser, ocf, path);
                        opfParser.addXMLHandler(opfHandler);

                        // add relaxNG validator
                        opfParser.addValidator(opfValidator);

                        // add schematron validator
                        opfParser.addValidator(opfSchematronValidator);

                        try {
                                // validate according to relaxNG + schematron
                                opfParser.process();
                        } catch (Throwable t) {
                                report.error(path, -1,
                                        "Failed performing OPF Schematron tests: " + t.getMessage());
                        }

                        if (!opfHandler.checkUniqueIdentExists()) {
                                report.error(path, -1,
                                        "unique-identifier attribute in package element must reference an existing identifier element id");
                        }

                        int refCount = opfHandler.getReferenceCount();
                        for (int i = 0; i < refCount; i++) {
                                OPFReference ref = opfHandler.getReference(i);
                                if (opfHandler.getItemByPath(ref.getHref()) == null) {
                                    report.error(path, ref.getLineNumber(), "File listed in reference element in guide was not declared in OPF manifest: " + ref.getHref());
                                }
                        }

                        int itemCount = opfHandler.getItemCount();
                        for (int i = 0; i < itemCount; i++) {
                                OPFItem item = opfHandler.getItem(i);
                                try {
                                        xrefChecker.registerResource(item.getPath(), item.getMimeType(), item.isInSpine(),
                                                                     checkItemFallbacks(item, opfHandler), checkImageFallbacks(item, opfHandler));
                                } catch (IllegalArgumentException e) {
                                        report.error(path, item.getLineNumber(), e.getMessage());
                                }
                                checkItem(item, opfHandler);
                        }

                        int spineItemCount = opfHandler.getSpineItemCount();
                        for (int i = 0; i < spineItemCount; i++) {
                                OPFItem item = opfHandler.getSpineItem(i);
                                checkSpineItem(item, opfHandler);
                        }

                        for (int i = 0; i < itemCount; i++) {
                                OPFItem item = opfHandler.getItem(i);
                                checkItemContent(item, opfHandler);
                        }

                        try {
                            Iterator filesIter = ocf.getFileEntries().iterator();
                            while (filesIter.hasNext()) {
                                String entry = (String)filesIter.next();

                                if (opfHandler.getItemByPath(entry) == null && ! entry.startsWith("META-INF/") && ! entry.startsWith("META-INF\\") &&  ! entry.equals("mimetype") && ! containerEntries.contains(entry)) {
                                    report.warning(null, -1,
                                                   "item (" + entry + ") exists in the zip file, but is not declared in the OPF file");
                                }
                            }

                            Iterator directoriesIter = ocf.getDirectoryEntries().iterator();
                            while (directoriesIter.hasNext()) {
                                String directory = (String)directoriesIter.next();
                                boolean hasContents = false;
                                filesIter = ocf.getFileEntries().iterator();
                                while (filesIter.hasNext()) {
                                    String file = (String)filesIter.next();
                                    if (file.startsWith(directory)) {
                                        hasContents = true;
                                    }
                                }
                                if (! hasContents) {
                                    report.warning(null, -1,
                                                   "zip file contains empty directory " + directory);
                                }

                            }

                        } catch (IOException e) {
                            report.error(null, -1, "Unable to read zip file entries.");
                        }

                        xrefChecker.checkReferences();
                }
        }

        static boolean isBlessedItemType(String type) {
                return type.equals("application/xhtml+xml")
                                || type.equals("application/x-dtbook+xml");

        }

        static boolean isDeprecatedBlessedItemType(String type) {
                return type.equals("text/x-oeb1-document") || type.equals("text/html");
        }

        static boolean isBlessedStyleType(String type) {
                return type.equals("text/css");
        }

        static boolean isDeprecatedBlessedStyleType(String type) {
                return type.equals("text/x-oeb1-css");
        }

        static boolean isBlessedImageType(String type) {
                return type.equals("image/gif") || type.equals("image/png")
                                || type.equals("image/jpeg") || type.equals("image/svg+xml");
        }

        private void checkItem(OPFItem item, OPFHandler opfHandler) {
                String mimeType = item.getMimeType();
                String fallback = item.getFallback();
                if (mimeType == null || mimeType.equals("")) {
                        // Ensures that media-type attribute is not empty
                        report.error(path, item.getLineNumber(),
                                        "empty media-type attribute");
                } else if (!mimeType.matches("[a-zA-Z0-9!#$&+-^_]+/[a-zA-Z0-9!#$&+-^_]+")) {
                        /*
                         * Ensures that media-type attribute has correct content. The
                         * media-type must have a type and a sub-type divided by '/' The
                         * allowable content for the media-type attribute is defined in
                         * RFC4288 section 4.2
                         */
                        report.error(path, item.getLineNumber(),
                                        "invalid content for media-type attribute");
                } else if (isDeprecatedBlessedItemType(mimeType)
                                || isDeprecatedBlessedStyleType(mimeType)) {
                        if (opfHandler.getOpf20PackageFile()
                                        && mimeType.equals("text/html"))
                                report.warning(path, item.getLineNumber(),
                                        "text/html is not appropriate for XHTML/OPS, use application/xhtml+xml instead");
                        else if (opfHandler.getOpf12PackageFile() && mimeType.equals("text/html"))
                                report.warning(path, item.getLineNumber(),
                                        "text/html is not appropriate for OEBPS 1.2, use text/x-oeb1-document instead");
                        else if (opfHandler.getOpf20PackageFile())
                                report.warning(path, item.getLineNumber(),
                                                "deprecated media-type '" + mimeType + "'");
                }
                if (opfHandler.getOpf12PackageFile() && fallback == null) {
                        if (isBlessedItemType(mimeType))
                                report.warning(path, item.getLineNumber(), "use of OPS media-type '"
                                        + mimeType + "' in OEBPS 1.2 context; use text/x-oeb1-document instead");
                        else if (isBlessedStyleType(mimeType))
                                report.warning(path, item.getLineNumber(), "use of OPS media-type '"
                                                + mimeType + "' in OEBPS 1.2 context; use text/x-oeb1-css instead");
                }
                if (fallback != null) {
                        OPFItem fallbackItem = opfHandler.getItemById(fallback);
                        if (fallbackItem == null)
                                report.error(path, item.getLineNumber(),
                                                "fallback item could not be found");
                }
                String fallbackStyle = item.getFallbackStyle();
                if (fallbackStyle != null) {
                        OPFItem fallbackStyleItem = opfHandler.getItemById(fallbackStyle);
                        if (fallbackStyleItem == null)
                                report.error(path, item.getLineNumber(),
                                        "fallback-style item could not be found");
                }
        }

        private void checkItemContent(OPFItem item, OPFHandler opfHandler) {
                String mimeType = item.getMimeType();
                String path = item.getPath();
                if (mimeType != null) {
                        ContentCheckerFactory checkerFactory;
                        if (item.isNcx())
                                checkerFactory = NCXCheckerFactory.getInstance();
                        else
                                checkerFactory = (ContentCheckerFactory) contentCheckerFactoryMap
                                                .get(mimeType);
                        if (checkerFactory == null)
                                checkerFactory = GenericContentCheckerFactory.getInstance();
                        if (checkerFactory != null) {
                                ContentChecker checker = checkerFactory.newInstance(ocf,
                                                report, path, mimeType, xrefChecker);
                                checker.runChecks();
                        }
                }
        }

        private void checkSpineItem(OPFItem item, OPFHandler opfHandler) {
                // These checks are okay to be done on <spine> items, but they really should be done on all
                // <manifest> items instead.  I am avoiding making this change now pending a few issue
                // resolutions in the EPUB Maint Working Group (e.g. embedded fonts not needing fallbacks).
                // [GC 11/15/09]
                String mimeType = item.getMimeType();
                if (mimeType != null) {
                        if (isBlessedStyleType(mimeType)
                                        || isDeprecatedBlessedStyleType(mimeType)
                                        || isBlessedImageType(mimeType))
                                report.error(path, item.getLineNumber(), "'" + mimeType
                                                + "' is not a permissible spine media-type");
                        else if (!isBlessedItemType(mimeType)
                                        && !isDeprecatedBlessedItemType(mimeType)
                                        && item.getFallback() == null)
                                report.error(path, item.getLineNumber(),
                                                "non-standard media-type '" + mimeType
                                                                + "' with no fallback");
                        else if (!isBlessedItemType(mimeType)
                                        && !isDeprecatedBlessedItemType(mimeType)
                                        && !checkItemFallbacks(item, opfHandler))
                                report.error(path, item.getLineNumber(),
                                                "non-standard media-type '" + mimeType
                                                                + "' with fallback to non-spine-allowed media-type");
                }
        }

        private boolean checkItemFallbacks(OPFItem item, OPFHandler opfHandler) {
                String fallback = item.getFallback();
                if (fallback != null) {
                        OPFItem fallbackItem = opfHandler.getItemById(fallback);
                        if (fallbackItem != null) {
                                String mimeType = fallbackItem.getMimeType();
                                if (mimeType != null) {
                                        if (isBlessedItemType(mimeType)
                                                        || isDeprecatedBlessedItemType(mimeType))
                                                return true;
                                        if (checkItemFallbacks(fallbackItem, opfHandler))
                                                return true;
                                }
                        }
                }
                String fallbackStyle = item.getFallbackStyle();
                if (fallbackStyle != null) {
                        OPFItem fallbackStyleItem = opfHandler.getItemById(fallbackStyle);
                        if (fallbackStyleItem != null) {
                                String mimeType = fallbackStyleItem.getMimeType();
                                if (mimeType != null) {
                                        if (isBlessedStyleType(mimeType)
                                                        || isDeprecatedBlessedStyleType(mimeType))
                                                return true;
                                }
                        }
                }
                return false;
        }

        private boolean checkImageFallbacks(OPFItem item, OPFHandler opfHandler) {
                String fallback = item.getFallback();
                if (fallback != null) {
                        OPFItem fallbackItem = opfHandler.getItemById(fallback);
                        if (fallbackItem != null) {
                                String mimeType = fallbackItem.getMimeType();
                                if (mimeType != null) {
                                        if (isBlessedImageType(mimeType))
                                                return true;
                                        if (checkImageFallbacks(fallbackItem, opfHandler))
                                                return true;
                                }
                        }
                }
                return false;
        }


}
