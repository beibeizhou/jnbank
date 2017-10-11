package com.centerm.iso8583.util;

import java.io.UnsupportedEncodingException;

import com.centerm.iso8583.ISOConfig;

public class ISOString {
	public static int length(String inString){
		
		byte[] b ={};
		try {
			b= inString.getBytes(ISOConfig.charSet);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return b.length;
	}
	public static String substring(String inString,int beginIdx,int length){
		byte[] b ={};
		try {
			b= inString.getBytes(ISOConfig.charSet);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(b.length < length){
			return null;
		}
		byte[] dest=new byte[length];
		System.arraycopy(b, beginIdx, dest, 0, length);
		try {
			return new String(dest,ISOConfig.charSet);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
	}
	
	
	public static void main(String[] args){
		
		String aa="12345678你好";
		
		System.out.println("aa.length="+length(aa)+"   "+substring(aa,0,19));
	}

}
