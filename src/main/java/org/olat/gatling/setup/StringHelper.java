/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.gatling.setup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Initial date: 17.12.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class StringHelper {

	private static final Logger log = LoggerFactory.getLogger(StringHelper.class);
	
	private static final Pattern WHITESPACE_PATTERN = Pattern.compile("^\\s*$");
	private static final int LONG_MAX_LENGTH = Long.toString(Long.MAX_VALUE).length();
	
	private static final DecimalFormat scoreFormat = new DecimalFormat("#0.###", new DecimalFormatSymbols(Locale.ENGLISH));
	
	public static final boolean equals(String s1, String s2) {
		if(s1 == null && s2 == null) {
			return true;
		}
		if(s1 != null && s1.equals(s2)) {
			return true;
		}
		
		if(s1 == null && s2 != null && s2.length() == 0) {
			return true;
		}
		if(s2 == null && s1 != null && s1.length() == 0) {
			return true;
		}
		
		return false;
	}
	
	public static final boolean equalsIgnoreCase(String s1, String s2) {
		if(s1 == null && s2 == null) {
			return true;
		}
		if(s1 != null && s1.equalsIgnoreCase(s2)) {
			return true;
		}
		
		if(s1 == null && s2 != null && s2.length() == 0) {
			return true;
		}
		if(s2 == null && s1 != null && s1.length() == 0) {
			return true;
		}
		
		return false;
	}
	
	public static final boolean equals(Date d1, Date d2) {
		if(d1 == null && d2 == null) {
			return true;
		}
		if(d1 != null && d1.equals(d2)) {
			return true;
		}
		
		if(d1 != null && d2 != null) {
			long t1 = d1.getTime();
			long t2 = d2.getTime();
			long diff = Math.abs(t2 - t1);
			if(diff < 60 * 1000) {
				return true;
			}	
		}
		return false;
	}

	public static final boolean equals(Float f1, Double f2, double tolerance) {
		if(f1 == null && f2 == null) {
			return true;
		}
		if(f1 != null && f2 != null) {
			double ff1 = f1.doubleValue();
			double ff2 = f2.doubleValue();
			double diff = Math.abs(ff1 - ff2);
			return tolerance > diff;
		}
		return false;
	}
	
	public static final String normalizeGender(String openolatValue) {
		if(openolatValue != null) {
			openolatValue = openolatValue.toLowerCase();
		}
		
		if("weiblich".equals(openolatValue) || "f\u00E9minin".equals(openolatValue) || "female".equals(openolatValue) || "f".equals(openolatValue)) {
			return "female";
		} else if("m\u00E4nnlich".equals(openolatValue) || "masculin".equals(openolatValue) || "male".equals(openolatValue) || "m".equals(openolatValue)) {
			return "male";
		} else if("-".equals(openolatValue)) {
			return "-";
		}
		return null;
	}
	
	/**
	 * Checks if a string has anything in it to display. Will return true if the
	 * string is not null and does contain at least one none-whitespace character.
	 * 
	 * @param s The String to be evaluated
	 * @return true if the string contains any non-whitespace character, false
	 *         otherwhise
	 */
	public static boolean containsNonWhitespace(String s) {
		if (s == null || s.length() == 0) return false;
		
		char firstChar = s.charAt(0);
		if(firstChar > 32 && firstChar < 127) {
			return true;
		}

		Matcher matcher = WHITESPACE_PATTERN.matcher(s);
		// if string matches whitespace pattern then string does not
		// contain non-whitespace
		return !matcher.find();
	}
	
	public static int toInt(String value, int def) {
		if(StringHelper.containsNonWhitespace(value)) {
			try {
				return Integer.parseInt(value);
			} catch (NumberFormatException e) {
				return def;
			}
		}
		return def;
	}
	
	/**
	 * Remove returns and tabs from the string
	 * @param value
	 * @return
	 */
	public static String clean(String value) {
		if(value == null) return value;
		
		String cleanedValue = value
				.replace('\n', ' ').replace('\r', ' ').replace('\t', ' ')
				.trim();
		return cleanedValue;
	}
	
	public static String cut(String value, int maxLength) {
		if(value == null || value.length() < maxLength) return value;
		return value.substring(0, maxLength - 1);
	}
	
	/**
	 * The method do only a precheck if the string can be a number. It's goal
	 * is to prevent to generate hunderds of exceptions in a loop by using
	 * the Long.parseLong() method (exceptions is time and CPU intensive).
	 * 
	 * return True if the string can be a digit (there is not boundaries check)
	 */
	public static boolean isLong(String string) {
		if(string == null || string.length() == 0) {
			return false;
		}
		int stop = string.startsWith("-") ? 1 : 0;
		if(string.length() > LONG_MAX_LENGTH + stop) {
			return false;
		}
		char[] charArr = string.toCharArray();
		for(int i=charArr.length; i-->stop; ) {
			char ch = charArr[i];
			if(ch < 48 || ch > 57) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean isInArray(String string, int[] array) {
		for(int i=array.length; i-->0; ) {
			String stringArr = Integer.toString(array[i]);
			if(stringArr.equals(string)) {
				return true;
			}
		}
		return false;
	}
	
	public static String toString(InputStream stream) {
		String results = null;
		try( BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
			results = reader.lines().collect(Collectors.joining("\n"));
		} catch(IOException e) {
			log.error("", e);
		}
		return results;
	}
	
	public static final String encodeBase64(byte[] bytes) {
		return Base64.getEncoder().encodeToString(bytes);
	}
	
	public static Long safelyLongValue(String val) {
		try {
			return Long.valueOf(val);
		} catch (NumberFormatException e) {
			log.error("", e);
			return null;
		}
	}
	
	/**
	 * @param score The score to be rounded
	 * @return The rounded score for GUI presentation
	 */
	public static String getRoundedScore(Float score) {
		if (score == null) return null;

		//cluster_OK the formatter is not multi-thread and costly to create
		synchronized(scoreFormat) {
			return scoreFormat.format(score);
		}
	}
	
	public static String getRoundedScore(Double score) {
		if (score == null) return null;

		//cluster_OK the formatter is not multi-thread and costly to create
		synchronized(scoreFormat) {
			return scoreFormat.format(score);
		}
	}
	
	public static Float getRoundedScore(String score) {
		if (!StringHelper.containsNonWhitespace(score)) return null;

		//cluster_OK the formatter is not multi-thread and costly to create
		synchronized(scoreFormat) {
			try {
				return Float.valueOf(scoreFormat.parse(score).floatValue());
			} catch (ParseException e) {
				log.error("", e);
				return null;
			}
		}
	}
	
	/**
	 * Test if the given {@code String} starts with the specified prefix,
	 * ignoring upper/lower case.
	 * @param str the {@code String} to check
	 * @param prefix the prefix to look for
	 * @see java.lang.String#startsWith
	 */
	public static boolean startsWithIgnoreCase(String str, String prefix) {
		return (str != null && prefix != null && str.length() >= prefix.length() &&
				str.regionMatches(true, 0, prefix, 0, prefix.length()));
	}
	
	public static Collection<List<String>> collectionOfChunks(List<String> list, int chunkSize) {
		AtomicInteger counter = new AtomicInteger();
		return list.stream()
			.collect(Collectors.groupingBy(it -> counter.getAndIncrement() / chunkSize))
			.values();
	}
}
