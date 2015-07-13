package org.easetech.processor;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashGenerator {

	
	private static MessageDigest md;


	public static String generateHash(String input, String algo) {
		MessageDigest mDigest = getMd(algo);
		StringBuffer sb = new StringBuffer();
		if(mDigest != null) {
			byte[] result = mDigest.digest(input.getBytes());
			
			
			for (int i = 0; i < result.length; i++) {
				sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16)
						.substring(1));
			}
		}
		

		return sb.toString();
	}


	public static MessageDigest getMd(String algo) {
		if(md != null) {
			return md;
		}
		try {
			md = MessageDigest.getInstance(algo);
		} catch (NoSuchAlgorithmException e) {
			if("SHA-256".equals(algo)) {
				return null;
			}
			System.out.println("The specified algorithm "+ algo+" is not recognizable.Defaulting to SHA-256");
			getMd("SHA-256");
			
		}
		return md;
	}

}
