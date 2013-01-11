package com.adobe.epubcheck.opf;

import com.adobe.epubcheck.util.EPUBVersion;

public class OPFDataImpl implements OPFData {

	private final EPUBVersion version;
	
	public OPFDataImpl(EPUBVersion version) {
		this.version = version;
	}

	@Override
	public EPUBVersion getVersion() {
		return version;
	}

	@Override
	public String getUniqueIdentifier() {
		throw new UnsupportedOperationException("OPF ID peeking is not implemented.");
	}

}
