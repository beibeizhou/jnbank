package com.centerm.jnbank.printer;

import android.graphics.Bitmap;
import android.os.RemoteException;
import android.util.Log;

import com.centerm.cpay.midsdk.dev.define.IPrinterDev;
import com.centerm.cpay.midsdk.dev.define.printer.EnumPrinterStatus;
import com.centerm.cpay.midsdk.dev.define.printer.PrintListener;
import com.centerm.cpay.midsdk.dev.define.printer.PrinterDataItem;
import com.centerm.cpay.midsdk.dev.define.printer.task.BitmapTask;
import com.centerm.cpay.midsdk.dev.define.printer.task.PrintTask;
import com.centerm.cpay.midsdk.dev.define.printer.task.StringTask;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 打印机打印方法封装
 */
public class CpayPrintHelper {
	private IPrinterDev printer;
	private PrintTask printTask;
	private List<PrinterDataItem> printData = new ArrayList<PrinterDataItem>();

	public CpayPrintHelper(IPrinterDev printer) {
		super();
		this.printer = printer;
	}
	
	public void init(){
		printTask = new PrintTask();
		printData.clear();
	}
	
	public void release(){
		printData.clear();
		printTask = null;
	}
	// 字符串
	public void addString(String str) throws RemoteException {
		addString(str, PrinterDataItem.Align.LEFT, 8, false, null);
	}
	
	public void addString(String str, PrinterDataItem.Align align) throws RemoteException {
		addString(str, align, 8, false, null);
	}
	
	public void addString(String str, int size) throws RemoteException {
		addString(str, PrinterDataItem.Align.LEFT, size, false, null);

	}
	
	public void addString(String str, PrinterDataItem.Align align, int size) throws RemoteException {
		addString(str, align, size, false, null);
	}
	
	public void addString(String str, PrinterDataItem.Align align, int size, boolean isBold) throws RemoteException {
		addString(str, align, size, isBold, null);
	}

	public void addString(String str, PrinterDataItem.Align align, boolean isBold, boolean isWordWrap) throws RemoteException {
		PrinterDataItem dataItem = new PrinterDataItem(str);
		dataItem.setFontSize(8);
		dataItem.setAlign(align);
		dataItem.setBold(isBold);
		dataItem.setWordWrap(isWordWrap);
		printData.add(dataItem);
	}

	public void addString(String str, PrinterDataItem.Align align, int size, boolean isBold, PrintListener callback) throws RemoteException {
		Log.d("打印数据：", str.trim());
		PrinterDataItem dataItem = new PrinterDataItem(str.trim());
		dataItem.setFontSize(size);
		dataItem.setAlign(align);
		printData.add(dataItem);
	}
	public void addPrinterTask(Bitmap bitmap, int leftOffSet, int width, int height){
		BitmapTask bitmapTask = new BitmapTask(bitmap, leftOffSet, width, height);
		printTask.addTask(bitmapTask);
	}

	public void reSetTast(){
		List<PrinterDataItem> tmpData = new ArrayList<>();
		tmpData.addAll(printData);
		StringTask stringTask = new StringTask(tmpData);
		printTask.addTask(stringTask);
		printData.clear();
	}


	public void print(PrintListener callback) throws RemoteException {
		if (callback == null) {
			callback = new PrinterCallback();
		}
		StringTask stringTask = new StringTask(printData);
		printTask.addTask(stringTask);
		printer.print(printTask,callback);
	}
	// 换行
	public void printNewLine(int num) throws RemoteException {
		for (int i = 0; i < num; i++) {
			addString(" ");
		}
	}

	public void addItem(String str1, String str2, String str3,
						  int pLen, int cLen, int tLen, int size, boolean needLF) throws RemoteException, UnsupportedEncodingException {
		List<byte[]> productBytes = string2ByteArr(str1, pLen);
		// 打印第一行
		byte[] bytes = bytesMerge(productBytes.get(0),
				getBytesRight(str2, cLen, "gb2312"),
				getBytesRight(str3, tLen, "gb2312"));
		addBytes(bytes);
		// 名字太长则打印多行
		for (int i = 1; i < productBytes.size(); i++) {
			addBytes(productBytes.get(i));
		}
		// 换行符
		if (needLF) {
			printNewLine(1);
		}
	}

	private class PrinterCallback implements PrintListener {

		@Override
		public void onFinish() {

		}

		@Override
		public void onError(int i, String s) {

		}
	}


	/**
	 * 切纸
	 */
	public void feedPaper() throws RemoteException {
		printer.goPaper(2);
	}

	public EnumPrinterStatus getStatus() throws RemoteException {
		return printer.getPrinterStatus();
	}


	/**
	 * 将字符串拆分成字节数组, 不足补空格
	 * @param str
	 * @param len
	 * @return
	 * @throws IOException
	 */
	public List<byte[]> string2ByteArr(String str, int len) throws UnsupportedEncodingException {
		if (len == 0 || str.length() == 0){
			return null;
		}
		byte[] bytes = str.getBytes("gb2312");
		List<byte[]> result =  new ArrayList<byte[]>();
		int start = 0; // 初始起始位置
		int end = bytes.length < len ? bytes.length - 1: len - 1; // 初始结束位置
		int p = 0; // 起始指针
		int count = 0; // 中文字节计数
		while (p <= end) {
			if (bytes[p] > 127 || bytes[p] < 0) {
				count++;
			}
			if (p == end) {
				if (count % 2 != 0) {// 中文被拆分
					end -= 1;// 减少一个字节
				}
				if (end- start + 1 <= len) {
					int spaceNum = len - end + start - 1;
					byte[] spaces = new byte[spaceNum];
					Arrays.fill(spaces, 0, spaceNum, (byte)32); // 填充空格
					result.add(bytesMerge(Arrays.copyOfRange(bytes, start, end + 1), spaces));
				} else {
					result.add(Arrays.copyOfRange(bytes, start, len));
				}
				start = end + 1;
				p = start;
				end = end + len > bytes.length - 1 ? bytes.length - 1 : end + len;
				count = 0;
			} else {
				p++;
			}
		}

		return result;
	}

	public byte[] bytesMerge(byte[] ...arrs){
		if (arrs.length == 0){
			return null;
		}else if (arrs.length == 1){
			return arrs[0];
		}
		byte[] result = new byte[1024];
		int len = 0;
		for (byte[] bs : arrs) {
			System.arraycopy(bs, 0, result, len, bs.length);
			len += bs.length;
		}
		return Arrays.copyOfRange(result, 0, len);
	}

	// 获取字节数组， 不足补空格, 超过截取
	public byte[] getBytesRight(String str, int len, String charset) throws RemoteException, UnsupportedEncodingException {
		int strLen = str.getBytes(charset).length;
		if (strLen >= len) {
			return Arrays.copyOfRange(str.getBytes(charset),strLen - len, strLen);
		}
		byte[] bytes = new byte[len];
		Arrays.fill(bytes, 0, len - str.length(), (byte)32);
		System.arraycopy(str.getBytes(charset), 0, bytes, len - strLen, strLen);
		return bytes;
	}

	// 字节数组
	public void addBytes(byte[] bytes) throws RemoteException, UnsupportedEncodingException {
		addString(new String(bytes, "gb2312"));
	}

	public String printFillSpace(String str, int length){

		int str_length=length(str);
		if(str_length<length){
			for(;str_length<length;length--){
				str = " "+str;
			}
		}else{
			str = str.substring(0, length);
		}
		return str;
	}

	//计算字符串中含中文字符串长度
	public  int length(String value) {
		int valueLength = 0;
		String chinese = "[\u0391-\uFFE5]";
        /* 获取字段值的长度，如果含中文字符，则每个中文字符长度为2，否则为1 */
		for (int i = 0; i < value.length(); i++) {
            /* 获取一个字符 */
			String temp = value.substring(i, i + 1);
            /* 判断是否为中文字符 */
			if (temp.matches(chinese)) {
                /* 中文字符长度为2 */
				valueLength += 2;
			} else {
                /* 其他字符长度为1 */
				valueLength += 1;
			}
		}
		return valueLength;
	}
}
