package org.example.judgehubcodesandbox.noSafe;

import java.util.ArrayList;

/**
 * @author mortal
 * @date 2024/8/17 22:15
 */
public class MemoryError {
	//	  new byte[10000]是创建了10000字节，那么new byte就是创建了一个字节
	public static void main(String[] args) {
		ArrayList<Byte[]> bytes = new ArrayList<>();
		while (true) {
			// 每次都创建 10000字节的内存
			bytes.add(new Byte[10000]);

		}
	}
}
