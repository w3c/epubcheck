package com.adobe.epubcheck.ocf;

import java.io.InputStream;

public class IDPFFontManglingFilter implements EncryptionFilter {

	OCFPackage ocf;
	
	public IDPFFontManglingFilter( OCFPackage ocf ) {
		this.ocf = ocf;
	}
	
	public boolean canDecrypt() {
		return ocf.getUniqueIdentifier() != null;
	}

	public InputStream decrypt(InputStream in) {
		// TODO implement this once we start to validate fonts
		return null;
	}

}
