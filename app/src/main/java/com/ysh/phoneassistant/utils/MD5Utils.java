package com.ysh.phoneassistant.utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Utils {

	private MD5Utils() {

	}

	public static String md5(String plainText) {
		
		byte[] secretBytes = null;
		try {
			secretBytes = MessageDigest.getInstance("MD5").digest(plainText.getBytes());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
		String md5Code = new BigInteger(1, secretBytes).toString(16);
		for (int i = 0; i < 32 - md5Code.length(); i++) {
			md5Code = "0" + md5Code;
		}
		return md5Code;
	}
}
