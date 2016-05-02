package com.ysh.phoneassistant.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class StreamTools {
	
	private StreamTools() {

	}
	
	public static String readFromInputStream(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buf = new byte[2048];
		int len = 0;
		
		while((len = in.read(buf)) != -1) {
			out.write(buf, 0, len);
		}
		
		in.close();
		out.flush();
		String result = out.toString();
		out.close();
		
		return result;
	}
}








