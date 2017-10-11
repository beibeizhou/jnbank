package com.centerm.iso8583.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.centerm.iso8583.ISOConfig;
/**
 * DES/3DES加解密的辅助类.
 * 完善补充工作者 Zhixiang Liu
 * 完善时间 2012.05.07
 * @原作者 Jianping Wang
 */
public class SecurityUtil {
	
	private static final String ALGORITHM = "DES";
	private static SecurityUtil util = new SecurityUtil();
	
	public static SecurityUtil getInstance() {
		return util;
	}
	
	/**
	 * DES加密
	 * @param key 加密密钥
	 * @param source 明文
	 * @return string 密文(16进制数字符串)
	 */
	public String encryptDES(String key, String source) {
		return bcd2str(encryptDes(hexStringToByte(key.toUpperCase()), hexStringToByte(source.toUpperCase())));
	}
	
	/**
	 * DES加密
	 * @param keybyte 加密密钥
	 * @param src 明文
	 * @return byte[] 密文
	 */
	public static byte[] encryptDes(byte[] keybyte, byte[] src) {
		try {
			// 生成密钥
			SecretKey deskey = new SecretKeySpec(keybyte, ALGORITHM);
			// 加密
			Cipher cipher = Cipher.getInstance(ALGORITHM + "/ECB/NoPadding");
			cipher.init(Cipher.ENCRYPT_MODE, deskey);
			return cipher.doFinal(src);
		} 
		catch (Exception e3) {
			e3.printStackTrace();
		}
		return null;
	}
	
	/**
	 * DES解密
	 * @param key 解密密钥
	 * @param source 密文
	 * @return string 明文(16进制数字符串)
	 */
	public static String decryptDES(String key, String source) {
		return bcd2str(decryptDes(hexStringToByte(key.toUpperCase()), hexStringToByte(source.toUpperCase())));
	}
	
	/**
	 * DES解密
	 * @param key 解密密钥
	 * @param source 密文
	 * @return byte[] 明文
	 */
	public static byte[] decryptDes(byte[] keybyte, byte[] src) {
		try {
			// 生成密钥
			SecretKey deskey = new SecretKeySpec(keybyte, ALGORITHM);
			// 解密
			Cipher cipher = Cipher.getInstance(ALGORITHM + "/ECB/NoPadding");
			cipher.init(Cipher.DECRYPT_MODE, deskey);
			return cipher.doFinal(src);
		} 
		catch (Exception e3) {
			e3.printStackTrace();
		}
		return null;
	}

	/**
	 * 3DES加密
	 * @param key 加密密钥(16字节长度)
	 * @param source 明文
	 * @return string 密文(16进制数字符串)
	 */
	public String encrype3DES(String key, String source) {
		return bcd2str(encrype3Des(hexStringToByte(key.toUpperCase()), hexStringToByte(source.toUpperCase())));
	}
	
	/**
	 *3DES加密
	 * @param key 加密密钥(16字节长度)
	 * @param source 明文
	 * @return byte[] 密文
	 */
	public static byte[] encrype3Des(byte[] key, byte[] source) {
		//初始化加密数据块
		byte[] cursorSourceBytes = new byte[8];
		System.arraycopy(source, 0, cursorSourceBytes, 0, 8);
		//初始化左半部分密钥
		byte[] keyLeft = new byte[8];
		System.arraycopy(key, 0, keyLeft, 0, 8);
		//初始化右半部分密钥
		byte[] keyRight = new byte[8];
		System.arraycopy(key, 8, keyRight, 0, 8);
		//第一步 : 用左半部分密钥对数据进行DES加密
		byte[] encryptResultBytes = encryptDes(keyLeft, cursorSourceBytes);
		//第二步 : 用右半部分密钥对第一步加密结果进行DES解密
		byte[] decryptResultbytes = decryptDes(keyRight, encryptResultBytes);
		//第三步 : 用左半部分密钥对第三步解密结果进行DES加密
		byte[] cursorResultBytes = encryptDes(keyLeft, decryptResultbytes);
		if(source.length>8) {//判断是否有多个8字节数据块
			//初始化下一个数据块
			byte[] tempSourceBytes = new byte[source.length-8];
			System.arraycopy(source, 8, tempSourceBytes, 0, source.length-8);
			//下一个数据库加密结果
			byte[] subRelultBytes = encrype3Des(key, tempSourceBytes);
			byte[] resultBytes = new byte[cursorResultBytes.length + subRelultBytes.length];
			//合并加密结果
			System.arraycopy(cursorResultBytes, 0, resultBytes, 0, cursorResultBytes.length);
			System.arraycopy(subRelultBytes, 0, resultBytes, cursorResultBytes.length, subRelultBytes.length);
			return resultBytes;
		}
		return cursorResultBytes;
	}
	
	/**
	 * 3DES解密
	 * @param key 解密密钥(16字节长度)
	 * @param source 密文
	 * @return string 明文(16进制数字符串)
	 */
	public static String decrypt3DES(String key, String source) {
		return bcd2str(decrypt3Des(hexStringToByte(key.toUpperCase()), hexStringToByte(source.toUpperCase())));
	}
	
	/**
	 * 3DES解密
	 * @param key 解密密钥(16字节长度)
	 * @param source 密文
	 * @return byte[] 明文
	 */
	public static byte[] decrypt3Des(byte[] key, byte[] source) {
		//将16字节密钥分解为各8字节的两个子密钥
		byte[] keyleft = new byte[8];
		System.arraycopy(key, 0, keyleft, 0, 8);
		byte[] keyright = new byte[8];
		System.arraycopy(key, 8, keyright, 0, 8);		
		//初始化当前密文
		byte[] cursorSrouceBytes = new byte[8];
		System.arraycopy(source, 0, cursorSrouceBytes, 0, 8);
		//加密步骤一：第一个子密钥对当前密文解密
		byte[] leftencrypt1 = decryptDes(keyleft, cursorSrouceBytes);
		//加密步骤二：第二个子密钥对步骤一加密结果进行加密
		byte[] rightdecrypt2 = encryptDes(keyright, leftencrypt1);
		//加密步骤三A：第一个子密钥对步骤三结果进行解密
		byte[] leftencrypt3 = decryptDes(keyleft, rightdecrypt2);
		if(source.length>8) {//判断是否含下一个8字节密文数据块
			//初始化下一个密文数据块
			byte[] subSourceBytes = new byte[source.length-8];
			System.arraycopy(source, 8, subSourceBytes, 0, source.length-8);
			//下一个密文数据库解密结果
			byte[] subResultBytes = decrypt3Des(key, subSourceBytes);
			//生成解密结果
			byte[] resultBytes = new byte[subResultBytes.length + leftencrypt3.length];
			System.arraycopy(leftencrypt3, 0, resultBytes, 0, leftencrypt3.length);
			System.arraycopy(subResultBytes, 0, resultBytes, leftencrypt3.length, subResultBytes.length);
			return resultBytes;
		}		
		return leftencrypt3;
	}
	
	/**
	 * ANSI-X9.9标准下DES算法的MAC计算
	 * @param source 源数据
	 * @param key 加密密钥
	 * @param vector 初始向量
	 * @param isHex 标志源数据是否以字符串表示的16进制形式进行操作
	 * @return string mac值(16进制数字符串)
	 * @throws Exception
	 */
	public String ansiMacDES(String key, String vector, String source, boolean isHex) throws Exception {
		return mac(isHex?hexStringToByte(source.toUpperCase()):source.getBytes(ISOConfig.charSet), hexStringToByte(key.toUpperCase()), hexStringToByte(vector.toUpperCase()));
	}
	
	/**
	 * DES算法的MAC计算
	 * @param source 源数据
	 * @param key 加密密钥
	 * @param vector 初始向量
	 * @return string mac值(16进制数字符串)
	 * @throws Exception
	 */
	public String mac(byte[] source, byte[] key, byte[] vector) throws Exception {
		byte[] cursorSourceBytes = new byte[8];
		System.arraycopy(source, 0, cursorSourceBytes, 0, (source.length>8 ? 8:source.length));
		byte[] sourceLeftXor = xor(cursorSourceBytes, vector);
		byte[] sourceLeftEncrypt = encryptDes(key, sourceLeftXor);
		if(source.length>8) {
			byte[] tempBytes = new byte[source.length - 8];
			System.arraycopy(source, 8, tempBytes, 0, source.length-8);
			return mac(tempBytes, key, sourceLeftEncrypt);
		}
		return bcd2str(sourceLeftEncrypt);
	}
	
	/**
	 * PBOC标准下DES算法的MAC计算
	 * @param source 源数据
	 * @param key 加密密钥
	 * @param vector 初始向量
	 * @param isHex 标志源数据是否以字符串表示的16进制形式进行操作
	 * @return string mac值(16进制数字符串)
	 * @throws Exception
	 */
	public String pbocMacDES(String key, String vector, String source, boolean isHex) throws Exception {
		byte[] sourceFilledBytes = fillBytes(isHex ? hexStringToByte(source.toUpperCase()) : source.getBytes(ISOConfig.charSet));
		return mac(sourceFilledBytes, hexStringToByte(key.toUpperCase()), hexStringToByte(vector.toUpperCase()));
	}
	
	/**
	 * 根据PBOC标准对字节补位
	 * @param sourceBytes 需要补充的byte数组
	 * @return byte[] 补充完毕的byte数组
	 */
	private byte[] fillBytes(byte[] sourceBytes) {
		int mod = sourceBytes.length%8;
		byte[] sourceFilledBytes = new byte[sourceBytes.length + (8-mod)];
		System.arraycopy(sourceBytes, 0, sourceFilledBytes, 0, sourceBytes.length);
		if(mod==0) {
			byte[] fillBytes = hexStringToByte("8000000000000000");
			System.arraycopy(fillBytes, 0, sourceFilledBytes, sourceBytes.length, fillBytes.length);		
		}else {
			for(int i=0; i<(8-mod);i++) {
				sourceFilledBytes[sourceBytes.length + i] = hexStringToByte(i==0?"80":"00")[0];
			}
		}
		return sourceFilledBytes;
	}
	
	/**
	 * ANSI-X9.9标准下3DES算法的MAC计算
	 * @param source 源数据
	 * @param key 加密密钥
	 * @param vector 初始向量
	 * @param isHex 标志源数据是否以字符串表示的16进制形式进行操作
	 * @return string mac值(16进制数字符串)
	 * @throws Exception
	 */
	public String ansiMac3DES(String source, String key, String vector, boolean isHex) throws Exception {
		return mac3Des(isHex?hexStringToByte(source.toUpperCase()):source.getBytes(ISOConfig.charSet), hexStringToByte(key.toUpperCase()), hexStringToByte(vector.toUpperCase()));
	}
	
	/**
	 * PBOC标准下3DES算法的MAC计算
	 * @param source 源数据
	 * @param key 加密密钥
	 * @param vector 初始向量
	 * @param isHex 标志源数据是否以字符串表示的16进制形式进行操作
	 * @return string mac值(16进制数字符串)
	 * @throws Exception
	 */
	public String pbocMac3DES(String key, String vector, String source, boolean isHex) throws Exception {
		byte[] sourceFilledBytes = fillBytes(isHex ? hexStringToByte(source.toUpperCase()) : source.getBytes(ISOConfig.charSet));
		return mac3Des(sourceFilledBytes, hexStringToByte(key.toUpperCase()), hexStringToByte(vector.toUpperCase()));
	}
	
	/**
	 * 3DES算法的MAC计算
	 * @param source 源数据
	 * @param key 加密密钥
	 * @param vector 初始向量
	 * @return string mac值(16进制数字符串)
	 * @throws Exception
	 */
	public String mac3Des(byte[] source, byte[] key, byte[] vector) throws Exception {
		byte[] cursorSourceBytes = new byte[8];
		System.arraycopy(source, 0, cursorSourceBytes, 0, (source.length>=8 ? 8:source.length));
		byte[] cursorSourceXor = xor(cursorSourceBytes, vector);
		if(source.length>8) {
			byte[] cursorKey = new byte[8];
			System.arraycopy(key, 0, cursorKey, 0, 8);
			byte[] sourceLeftEncrypt = encryptDes(cursorKey, cursorSourceXor);
			byte[] tempBytes = new byte[source.length - 8];
			System.arraycopy(source, 8, tempBytes, 0, source.length-8);
			return mac3Des(tempBytes, key, sourceLeftEncrypt);
		}
		return bcd2str(encrype3Des(key, cursorSourceXor));	
	}
	
	/**
	 * Diversify密钥分散算法
	 * @param source 源数据
	 * @param key 主控密钥MK
	 * @return string 分散密钥DK(16进制数字符串)
	 */
	public String diversify(String key, String source) {
		return bcd2str(diversify(hexStringToByte(key.toUpperCase()), hexStringToByte(source.toUpperCase())));
	}
	
	/**
	 * Diversify密钥分散算法
	 * @param source 源数据
	 * @param key 主控密钥MK
	 * @return byte[] 分散密钥DK(16进制数字符串)
	 */
	public byte[] diversify(byte[]key, byte[] source) {
		//当前分散数据
		byte[] cursorSourceBytes = new byte[8];
		System.arraycopy(source, 0, cursorSourceBytes, 0, source.length>8?8:source.length);	
		//推导左半部分key
		byte[] leftDivBytes = encrype3Des(key, cursorSourceBytes);
		//当前分散数据取反
		for (int i = 0; i < cursorSourceBytes.length; i++) {
			cursorSourceBytes[i] = (byte) ~ cursorSourceBytes[i];
		}
		//推导右半部分key
		byte[] rightDivBytes = encrype3Des(key, cursorSourceBytes);
		//合并
		byte[] resultBytes = new byte[leftDivBytes.length + rightDivBytes.length ];
		System.arraycopy(leftDivBytes, 0, resultBytes, 0, leftDivBytes.length);
		System.arraycopy(rightDivBytes, 0, resultBytes, leftDivBytes.length, rightDivBytes.length);	
		if(source.length>8) {//判断是否二次分散运算
			byte[] tempBytes = new byte[source.length-8];
			System.arraycopy(source, 8, tempBytes, 0, source.length-8);
			return diversify(resultBytes, tempBytes);
		}
		return resultBytes;
	}
	
	/**
	 * Double-One-Way分散算法
	 * @param source 源数据
	 * @param key 主控密钥MK
	 * @return string 分散密钥DK(16进制数字符串)
	 * @throws Exception
	 */
	public String diversifyByDoubleOneWay(String source, String key) throws Exception {
		return diversifyDouble(source, key);
	}
	
	/**
	 * Double-One-Way分散运算
	 * @param source 源数据
	 * @param key 主控密钥MK
	 * @return string 分散密钥DK(16进制数字符串)
	 * @throws Exception
	 */
	private String diversifyDouble(String source, String key) throws Exception {
		byte[] keyleft = hexStringToByte(key.substring(0, key.length()/2).toUpperCase());
		byte[] keyright = hexStringToByte(key.substring(key.length()/2).toUpperCase());
		byte[] sourceBytes = hexStringToByte(source.toUpperCase());
		byte[] sourceUnDes = decryptDes(keyleft, sourceBytes);
		byte[] sourceunDesDes = encryptDes(keyright, sourceUnDes);
		byte[] sourceunDesDesUnDes = decryptDes(keyleft, sourceunDesDes);
		byte[] keyleftXor = xor(sourceBytes, sourceunDesDesUnDes);
		return bcd2str(keyleftXor);
	}
		
	/**
	 * 异或运算
	 * @param xor1 操作数1
	 * @param xor2 操作数2
	 * @return string 异或结果(16进制数字符串)
	 * @throws Exception
	 */
	public String xor(String xor1, String xor2) throws Exception  {
		return bcd2str(xor(hexStringToByte(xor1), hexStringToByte(xor2)));
	}
	
	/**
	 * 异或运算
	 * @param xor1 操作数1
	 * @param xor2 操作数2
	 * @return byte[] 异或结果(16进制数字符串)
	 * @throws Exception
	 */
	public byte[] xor(byte[] hexSource1, byte[] hexSource2) throws Exception {
		int length =  hexSource1.length;
		byte[] xor = new byte[length];
		for (int i = 0; i < length; i++) {
			xor[i] = (byte) (hexSource1[i]^hexSource2[i]);
		}
		return xor;
	}
	
	/**
	 * 将字节转换成16进制数字符
	 * @param bcds 待转换的byte数组
	 * @return string 转换结果(字母都转为大写)
	 */
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

	/**
	 * 16进制字符串转换成字节数组
	 * @param hex 待转换的字符串
	 * @return byte[] 结果数组
	 */
	public static byte[] hexStringToByte(String hex) {
		int len = (hex.length() / 2);
		byte[] result = new byte[len];
		char[] achar = hex.toCharArray();
		for (int i = 0; i < len; i++) {
			int pos = i * 2;
			result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
		}
		return result;
	}

	/**
	 * ECB加密
	 * @param key 加密密钥
	 * @param source 明文
	 * @return string 密文(16进制数字符串)
	 */
	public String encryptECB(String key, String source) {
		return bcd2str(encryptECB(hexStringToByte(key.toUpperCase()), hexStringToByte(source.toUpperCase())));
	}
	
	/**
	 * ECB-3DES加密
	 * @param key 加密密钥
	 * @param source 明文
	 * @return byte[] 密文
	 */
	public byte[] encryptECB(byte[] key, byte[] source) {
		byte[] cursorSourntBytes = new byte[8];
		System.arraycopy(source, 0, cursorSourntBytes, 0, source.length>8?8:source.length);
		byte[] currorEncryptResult = key.length>8?encrype3Des(key, cursorSourntBytes):encryptDes(key, cursorSourntBytes);
		if(source.length>8) {
			byte[] nextSource = new byte[source.length-8];
			System.arraycopy(source, 8, nextSource, 0, source.length-8);
			byte[] subEncryptResult = encryptECB(key, nextSource);
			byte[] encryptResult =  new byte[currorEncryptResult.length + subEncryptResult.length];
			System.arraycopy(currorEncryptResult, 0, encryptResult, 0, currorEncryptResult.length);
			System.arraycopy(subEncryptResult, 0, encryptResult, currorEncryptResult.length, subEncryptResult.length);
			return encryptResult;
		}
		return currorEncryptResult;
	}
	
	/**
	 * ECB-3DES解密
	 * @param key 解密密钥
	 * @param source 密文
	 * @return String 明文
	 */
	public String decryptECB(String key, String source) {
		return bcd2str(decryptECB(hexStringToByte(key.toUpperCase()), hexStringToByte(source.toUpperCase())));
	}
	
	/**
	 * ECB-3DES解密
	 * @param key 解密密钥
	 * @param source 密文
	 * @return byte[] 明文
	 */
	public byte[] decryptECB(byte[] key, byte[] source) {
		byte[] cursorSourntBytes = new byte[8];
		System.arraycopy(source, 0, cursorSourntBytes, 0, source.length>8?8:source.length);
		byte[] currorDecryptResult = key.length>8?decrypt3Des(key, cursorSourntBytes):decryptDes(key, cursorSourntBytes);	
		if(source.length>8) {
			byte[] nextSource = new byte[source.length-8];
			System.arraycopy(source, 8, nextSource, 0, source.length-8);
			byte[] subEncryptResult = decryptECB(key, nextSource);		
			byte[] encryptResult =  new byte[currorDecryptResult.length + subEncryptResult.length];
			System.arraycopy(currorDecryptResult, 0, encryptResult, 0, currorDecryptResult.length);
			System.arraycopy(subEncryptResult, 0, encryptResult, currorDecryptResult.length, subEncryptResult.length);
			return encryptResult;
		}
		return currorDecryptResult;
	}
	
	/**
	 * CBC-3DES加密
	 * @param key 加密密钥
	 * @param vector 初始向量
	 * @param source 明文
	 * @return string 密文
	 * @throws Exception
	 */
	public String encryptCBC(String key, String vector, String source) throws Exception {
		return bcd2str(encryptCBC(hexStringToByte(key.toUpperCase()), hexStringToByte(vector!=null?vector:"0000000000000000"), hexStringToByte(source.toUpperCase())));
	}
	
	/**
	 * CBC-3DES加密
	 * @param key 加密密钥
	 * @param vector 初始向量
	 * @param source 明文
	 * @return byte[] 密文
	 * @throws Exception
	 */
	public byte[] encryptCBC(byte[] key, byte[] vector, byte[] source) throws Exception {
		byte[] cursorSourntBytes = new byte[8];
		System.arraycopy(source, 0, cursorSourntBytes, 0, source.length>8?8:source.length);		
		byte[] xorResultBytes = xor(cursorSourntBytes, vector);
		byte[] currorEncryptResult = key.length>8?encrype3Des(key, xorResultBytes):encryptDes(key, xorResultBytes);
		if(source.length>8) {
			byte[] nextSource = new byte[source.length-8];
			System.arraycopy(source, 8, nextSource, 0, source.length-8);
			byte[] subEncryptResult = encryptCBC(key, currorEncryptResult, nextSource);
			byte[] encryptResult =  new byte[currorEncryptResult.length + subEncryptResult.length];
			System.arraycopy(currorEncryptResult, 0, encryptResult, 0, currorEncryptResult.length);
			System.arraycopy(subEncryptResult, 0, encryptResult, currorEncryptResult.length, subEncryptResult.length);
			return encryptResult;
		}
		return currorEncryptResult;
	}
	
	/**
	 * CBC-3DES解密
	 * @param key 解密密钥
	 * @param vector 初始向量
	 * @param source 密文
	 * @return string 明文
	 * @throws Exception
	 */
	public String decryptCBC(String key, String vector, String source) throws Exception {
		return bcd2str(decryptCBC(hexStringToByte(key.toUpperCase()), hexStringToByte(vector!=null?vector:"0000000000000000"), hexStringToByte(source.toUpperCase())));
	}
	
	/**
	 * CBC-3DES解密
	 * @param key 解密密钥
	 * @param vector 初始向量
	 * @param source 密文
	 * @return byte[] 明文
	 * @throws Exception
	 */
	public byte[] decryptCBC(byte[] key, byte[] vector, byte[] source) throws Exception {
		byte[] decryptBytes = new byte[8];
		System.arraycopy(source, 0, decryptBytes, 0, source.length>8?8:source.length);
		
		byte[] decryptResult = key.length>8?decrypt3Des(key, decryptBytes):decryptDes(key, decryptBytes);
		byte[] result = xor(decryptResult, vector);
		if(source.length>8) {
			byte[] nextSource = new byte[source.length-8];
			System.arraycopy(source, 8, nextSource, 0, source.length-8);
			byte[] subDecryptResult = decryptCBC(key, decryptBytes, nextSource);
			byte[] encryptResult =  new byte[result.length + subDecryptResult.length];
			System.arraycopy(result, 0, encryptResult, 0, result.length);
			System.arraycopy(subDecryptResult, 0, encryptResult, result.length, subDecryptResult.length);
			return encryptResult;
		}
		return result;
	}
		
	/**
	 * 将字符转换为对应的16进制字节
	 * @param c 字符
	 * @return byte 对应字节
	 */
	private static byte toByte(char c) {
		byte b = (byte) "0123456789ABCDEF".indexOf(c);
		return b;
	}

	/**
	 * 检查名称为name的字符串value是否满足长度为length且每个字符是否都为16进制字符
	 * @param name 字符串名称
	 * @param value 字符串值
	 * @param length 字符串长度(小等于0时不进行长度校验)
	 * @return boolean
	 * @throws Exception
	 */
	public static boolean isHexademical(String name, String value, int length) throws Exception {
		 if(null==value || (value.length()!=length && length>0)) {
			 throw new Exception(name + "长度应为" + length);
		 }
		 String texts = "0123456789abcdefABCDEF";
		 int len = value.length();
		 for(int i=0; i<len; i++) {
			 if(texts.indexOf(value.charAt(i)) == -1) {
				 throw new Exception(name + "包含的字符应为16进制字符");
			 }
		 }
		 return true;
	}
	
	/**
	 * 检查名称为name的字符串value是否满足长度为length且每个字符是否都为字母或数字
	 * @param name 字符串名称
	 * @param value 字符串值
	 * @param length 字符串长度(小于0时不进行长度校验)
	 * @return boolean
	 * @throws Exception
	 */
	public static boolean isAlphanumeric(String name, String value, int length) throws Exception {
		 if(null==value || (value.length()!=length && length>0)) {
			 throw new Exception(name + "长度应为" + length);
		 }
		 String texts = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		 int len = value.length();
		 for(int i=0; i<len; i++) {
			 if(texts.indexOf(value.charAt(i)) == -1) {
				 throw new Exception(name + "包含的字符应为数字或字母");
			 }
		 }
		 return true;
	}
}
