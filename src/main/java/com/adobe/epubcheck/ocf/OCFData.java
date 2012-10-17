package com.adobe.epubcheck.ocf;

import java.util.List;

public interface OCFData {
	
	static final String containerEntry = "META-INF/container.xml";

	static final String encryptionEntry = "META-INF/encryption.xml";

	static final String signatureEntry = "META-INF/signatures.xml";

	/**
	 * @return the full paths of the root files of the container for the given
	 *         media type.
	 */
	public List<String> getEntries(String type);

	/**
	 * @return the full paths of all the root files of the container
	 */
	public List<String> getEntries();

}
