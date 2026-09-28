package ru.itmo.is.lab1.util;

import org.owasp.encoder.Encode;

public final class XssSanitizer {

	private XssSanitizer() {
	}

	public static String escape(String value) {
		if (value == null || value.isEmpty()) {
			return value;
		}
		return Encode.forHtml(value);
	}

}
