package com.adobe.epubcheck.util;

public final class KeyValue<A,B> {

	private final A key;
	private final B value;


	public static <A,B> KeyValue<A,B> with(final A key, final B value) {
		return new KeyValue<A,B>(key,value);
	}

	public KeyValue(final A key, final B value) {
		this.key = key;
		this.value = value;
	}

	public A getKey() {
		return this.key;
	}

	public B getValue() {
		return this.value;
	}

}
