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

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.ocf.OCFPackage;
import com.adobe.epubcheck.util.EPUBVersion;

public class XRefChecker {

	public static final int RT_GENERIC = 0;

	public static final int RT_HYPERLINK = 1;

	public static final int RT_IMAGE = 2;

	public static final int RT_OBJECT = 3;

	public static final int RT_STYLESHEET = 4;

	public static final int RT_AUDIO = 5;

	public static final int RT_VIDEO = 6;
	
	public static final int RT_SVG_PAINT = 0x10;

	public static final int RT_SVG_CLIP_PATH = 0x11;

	public static final int RT_SVG_SYMBOL = 0x12;

	private class Reference {
		String resource;

		int lineNumber;

		int columnNumber;

		String refResource;

		String fragment;

		int type;

		public Reference(String srcResource, int srcLineNumber,
				int srcColumnNumber, String refResource, String fragment,
				int type) {
			this.fragment = fragment;
			this.lineNumber = srcLineNumber;
			this.columnNumber = srcColumnNumber;
			this.refResource = refResource;
			this.resource = srcResource;
			this.type = type;
		}

	}

	private class Anchor {

		String id;

		int lineNumber;

		int columnNumber;

		int type;

		public Anchor(String id, int lineNumber, int columnNumber, int type) {
			this.id = id;
			this.lineNumber = lineNumber;
			this.columnNumber = columnNumber;
			this.type = type;
		}

	}

	private class Resource {

		String resource;

		String mimeType;

		Hashtable<String, Anchor> anchors;

		boolean inSpine;

		boolean hasValidItemFallback;

		boolean hasValidImageFallback;

		Resource(String resource, String type, boolean inSpine,
				boolean hasValidItemFallback, boolean hasValidImageFallback) {
			this.mimeType = type;
			this.resource = resource;
			this.inSpine = inSpine;
			this.hasValidItemFallback = hasValidItemFallback;
			this.hasValidImageFallback = hasValidImageFallback;
			this.anchors = new Hashtable<String, Anchor>();
		}
	}

	Hashtable<String, Resource> resources = new Hashtable<String, Resource>();

	HashSet<String> undeclared = new HashSet<String>();

	Vector<Reference> references = new Vector<Reference>();

	Hashtable<String, String> bindings = new Hashtable<String, String>();

	Report report;

	OCFPackage ocf;

	EPUBVersion version;

	public XRefChecker(OCFPackage ocf, Report report, EPUBVersion version) {
		this.ocf = ocf;
		this.report = report;
		this.version = version;

	}

	public String getMimeType(String path) {
		return resources.get(path) != null ? resources.get(path).mimeType
				: null;
	}

	public Set<String> getBindingsMimeTypes() {
		return bindings.keySet();
	}

	public String getBindingHandlerSrc(String mimeType) {
		return bindings.get(mimeType);
	}

	public void registerBinding(String mimeType, String handlerSrc) {
		bindings.put(mimeType, handlerSrc);
	}

	public void registerResource(String resource, String mimeType,
			boolean inSpine, boolean hasValidItemFallback,
			boolean hasValidImageFallback) {
		if (resources.get(resource) != null)
			throw new IllegalArgumentException("duplicate resource: "
					+ resource);
		resources.put(resource, new Resource(resource, mimeType, inSpine,
				hasValidItemFallback, hasValidImageFallback));
	}

	public void registerAnchor(String resource, int lineNumber,
			int columnNumber, String id, int type) {
		Resource res = (Resource) resources.get(resource);
		if (res == null)
			throw new IllegalArgumentException("unregistered resource: "
					+ resource);
		if (res.anchors.get(id) != null)
			throw new IllegalArgumentException("duplicate id: " + id);
		res.anchors.put(id, new Anchor(id, lineNumber, columnNumber, type));
	}

	public void registerReference(String srcResource, int srcLineNumber,
			int srcColumnNumber, String refResource, String refFragment,
			int type) {
		if (refResource.startsWith("data:"))
			return;		
		references.add(new Reference(srcResource, srcLineNumber,
				srcColumnNumber, refResource, refFragment, type));
	}

	public void registerReference(String srcResource, int srcLineNumber,
			int srcColumnNumber, String ref, int type) {
		if (ref.startsWith("data:"))
			return;
		// check for query string (http://code.google.com/p/epubcheck/issues/detail?id=190)
		int query = ref.indexOf('?');
		if (query >= 0) {
			ref = ref.substring(0, query).trim();
		}
		
		int hash = ref.indexOf("#");
		String refResource;
		String refFragment;
		if (hash >= 0) {
			refResource = ref.substring(0, hash);
			refFragment = ref.substring(hash + 1);
		} else {
			refResource = ref;
			refFragment = null;
		}
		registerReference(srcResource, srcLineNumber, srcColumnNumber,
				refResource, refFragment, type);
	}

	public void checkReferences() {
		Enumeration<Reference> refs = references.elements();
		while (refs.hasMoreElements()) {
			Reference ref = (Reference) refs.nextElement();
			checkReference(ref);
		}

	}

	private void checkReference(Reference ref) {
		Resource res = (Resource) resources.get(ref.refResource);
		if (res == null) {
			if((ref.refResource.startsWith("http://") || ref.refResource.startsWith("https://")) 
					&& !(version==EPUBVersion.VERSION_3 && (ref.type==RT_AUDIO || ref.type==RT_VIDEO))) {
				report.error(
						ref.resource,
						ref.lineNumber,
						ref.columnNumber,
						"'"
								+ ref.refResource
								+ "': remote resource reference not allowed; resource must be placed in the OCF");
			} else if (!ocf.hasEntry(ref.refResource) && !ref.refResource.startsWith("http://")) {				
				report.error(
						ref.resource,
						ref.lineNumber,
						ref.columnNumber,
						"'"
								+ ref.refResource
								+ "': referenced resource missing in the package.");
				
			} else if (!undeclared.contains(ref.refResource)) {
				undeclared.add(ref.refResource);
				report.error(
						ref.resource,
						ref.lineNumber,
						ref.columnNumber,
						"'"
								+ ref.refResource
								+ "': referenced resource is not declared in the OPF manifest.");
			}
			return;
		}
		if (ref.fragment == null) {
			switch (ref.type) {
			case RT_SVG_PAINT:
			case RT_SVG_CLIP_PATH:
			case RT_SVG_SYMBOL:
				report.error(ref.resource, ref.lineNumber, ref.columnNumber,
						"fragment identifier missing in reference to '"
								+ ref.refResource + "'");
				break;
			case RT_HYPERLINK:
				// if mimeType is null, we should have reported an error already
				if (res.mimeType != null
						&& !OPFChecker.isBlessedItemType(res.mimeType, version)
						&& !OPFChecker
								.isDeprecatedBlessedItemType(res.mimeType)
						&& !res.hasValidItemFallback)
					report.error(ref.resource, ref.lineNumber,
							ref.columnNumber,
							"hyperlink to non-standard resource '"
									+ ref.refResource + "' of type '"
									+ res.mimeType + "'");
				if (/* !res.mimeType.equals("font/opentype") && */!res.inSpine)
					report.warning(ref.resource, ref.lineNumber,
							ref.columnNumber,
							"hyperlink to resource outside spine '"
									+ ref.refResource + "'");
				break;
			case RT_IMAGE:
				// if mimeType is null, we should have reported an error already
				if (res.mimeType != null
						&& !OPFChecker.isBlessedImageType(res.mimeType)
						&& !res.hasValidImageFallback)
					report.error(ref.resource, ref.lineNumber,
							ref.columnNumber, "non-standard image resource '"
									+ ref.refResource + "' of type '"
									+ res.mimeType + "'");
				break;
			case RT_STYLESHEET:
				// if mimeType is null, we should have reported an error already

				// The original code is below, but we were never collecting
				// references to RT_STYLESHEETs; now we are.
				// Implementations are allowed to process any stylesheet
				// language they desire; so this is clearly not an
				// error. Making this a warning with "(might be ignored)" could
				// be okay. However, related, the OPF
				// Checker currently looks at only the <spine> to make sure
				// referneced items have appropiate fallbacks;
				// it should really be checking the <manifest>. If this was
				// corrected, these alternate stylesheet
				// items (with non-blessed MIME types) would likely get flagged
				// as missing requried fallbacks. Flagging
				// this during manifest processing seems the right choice, so,
				// commenting out for now. [GC 11/15/09]

				// if (res.mimeType != null
				// && !OPFChecker.isBlessedStyleType(res.mimeType)
				// && !OPFChecker
				// .isDeprecatedBlessedStyleType(res.mimeType))
				// report.error(ref.resource, ref.lineNumber,
				// "non-standard stylesheet resource '"
				// + ref.refResource + "' of type '"
				// + res.mimeType + "'");
				break;
			}
		} else { //if (ref.fragment == null) {
			
			if(ref.fragment.startsWith("epubcfi(")) {
				//Issue 150
				return;
			}
			
			switch (ref.type) {
			case RT_HYPERLINK:
				// if mimeType is null, we should have reported an error already
				if (res.mimeType != null
						&& !OPFChecker.isBlessedItemType(res.mimeType, version)
						&& !OPFChecker
								.isDeprecatedBlessedItemType(res.mimeType)
						&& !res.hasValidItemFallback)
					report.error(ref.resource, ref.lineNumber,
							ref.columnNumber,
							"hyperlink to non-standard resource '"
									+ ref.refResource + "' of type '"
									+ res.mimeType + "'");
				if (!res.inSpine)
					report.warning(ref.resource, ref.lineNumber,
							ref.columnNumber,
							"hyperlink to resource outside spine '"
									+ ref.refResource + "'");
				break;
			case RT_IMAGE:
				report.error(ref.resource, ref.lineNumber, ref.columnNumber,
						"fragment identifier used for image resource '"
								+ ref.refResource + "'");
				break;
			case RT_STYLESHEET:
				report.error(ref.resource, ref.lineNumber, ref.columnNumber,
						"fragment identifier used for stylesheet resource '"
								+ ref.refResource + "'");
				break;
			}
			Anchor anchor = (Anchor) res.anchors.get(ref.fragment);
			if (anchor == null) {
				report.error(ref.resource, ref.lineNumber, ref.columnNumber,
						"'" + ref.fragment
								+ "': fragment identifier is not defined in '"
								+ ref.refResource + "'");
				return;
			} else {
				switch (ref.type) {
				case RT_SVG_PAINT:
				case RT_SVG_CLIP_PATH:
					if (anchor.type != ref.type)
						report.error(
								ref.resource,
								ref.lineNumber,
								ref.columnNumber,
								"fragment identifier '"
										+ ref.fragment
										+ "' defines incompatible resource type in '"
										+ ref.refResource + "'");
					break;
				case RT_SVG_SYMBOL:
				case RT_HYPERLINK:
					if (anchor.type != ref.type && anchor.type != RT_GENERIC)
						report.error(
								ref.resource,
								ref.lineNumber,
								ref.columnNumber,
								"fragment identifier '"
										+ ref.fragment
										+ "' defines incompatible resource type in '"
										+ ref.refResource + "'");
					break;
				}
			}
		}
	}
}
