package com.centerm.iso8583.util;


public class M3HexUtil {
	public static byte[] hexStringToByte(String hex) {
		hex = hex.toUpperCase();
		int len = (hex.length() / 2);
		byte[] result = new byte[len];
		char[] achar = hex.toCharArray();
		for (int i = 0; i < len; i++) {
			int pos = i * 2;
			result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
		}
		return result;
	}


	public static String bcd2str(byte[] bcds) {
		char[] ascii = "0123456789abcdef".toCharArray();
		byte[] temp = new byte[bcds.length * 2];
		for (int i = 0; i < bcds.length; i++) {
			temp[i * 2] = (byte) ((bcds[i] >> 4) & 0x0f);
			temp[i * 2 + 1] = (byte) (bcds[i] & 0x0f);
		}
		StringBuffer res = new StringBuffer();

		for (int i = 0; i < temp.length; i++) {
			res.append(ascii[temp[i]]);
		}
		return res.toString().toUpperCase();
	}

	private static byte toByte(char c) {
		byte b = (byte) "0123456789ABCDEF".indexOf(c);
		return b;
	}

	public static byte[] int2bytes(int num) {
		byte[] b = new byte[4];
		int mask = 0xff;
		for (int i = 0; i < 4; i++) {
			b[i] = (byte) (num >>> (24 - i * 8));
		}
		return b;
	}

	public static int bytes2int(byte[] b) {
		// byte[] b=new byte[]{1,2,3,4};
		int mask = 0xff;
		int temp = 0;
		int res = 0;
		for (int i = 0; i < 4; i++) {
			res <<= 8;
			temp = b[i] & mask;
			res |= temp;
		}
		return res;
	}
	
    /** 
     * 将长度为2的byte数组转换为16位int 
     *  
     * @param res 
     *            byte[] 
     * @return int 
     * */  
	public static int bytes2short(byte[] b) {
		// byte[] b=new byte[]{1,2,3,4};
		int mask = 0xff;
		int temp = 0;
		int res = 0;
		for (int i = 0; i < 2; i++) {
			res <<= 8;
			temp = b[i] & mask;
			res |= temp;
		}
		return res;
	}

	public static String getBinaryStrFromByteArr(byte[] bArr) {
		String result = "";
		for (byte b : bArr) {
			result += getBinaryStrFromByte(b);
		}
		return result;
	}


	public static String getBinaryStrFromByte(byte b) {
		String result = "";
		byte a = b;
		;
		for (int i = 0; i < 8; i++) {
			byte c = a;
			a = (byte) (a >> 1);
			a = (byte) (a << 1);
			if (a == c) {
				result = "0" + result;
			} else {
				result = "1" + result;
			}
			a = (byte) (a >> 1);
		}
		return result;
	}

	public static String getBinaryStrFromByte2(byte b) {
		String result = "";
		byte a = b;
		;
		for (int i = 0; i < 8; i++) {
			result = (a % 2) + result;
			a = (byte) (a >> 1);
		}
		return result;
	}

	public static String getBinaryStrFromByte3(byte b) {
		String result = "";
		byte a = b;
		;
		for (int i = 0; i < 8; i++) {
			result = (a % 2) + result;
			a = (byte) (a / 2);
		}
		return result;
	}
	

    public static byte[] toByteArray(int iSource, int iArrayLen) { 
        byte[] bLocalArr = new byte[iArrayLen]; 
        for ( int i = 0; (i < 4) && (i < iArrayLen); i++) { 
            bLocalArr[i] = (byte)( iSource>>8*i & 0xFF ); 
           
        } 
        return bLocalArr; 
    }  
    
  
    public static byte[] xor(byte[] op1,byte[] op2){
    	if(op1.length!=op2.length){
    		throw new IllegalArgumentException("参数错误，长度不一致");
    	}
    	byte[] result = new byte[op1.length];
    	for(int i=0;i<op1.length;i++){
    		result[i] = (byte) (op1[i] ^ op2[i]);
    	}
    	return result;
    }
}
