package com.adobe.epubcheck.ocf;

import java.io.InputStream;

public class IDPFFontManglingFilter implements EncryptionFilter {

	String uniqueIdentifier;
	
	public IDPFFontManglingFilter( String Uid) {
		uniqueIdentifier = Uid;
	}
	
	public boolean canDecrypt() {
		return uniqueIdentifier != null;
	}

	public InputStream decrypt(InputStream in) {
		// TODO implement this once we start to validate fonts
		return null;
	}

}
