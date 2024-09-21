package org.example.judgehubcodesandbox;

/**
 * @author mortal
 * @date 2024/8/16 18:00
 */
public class SimpleCompute {
	public static void main(String[] args) {
		// 一般用到main方法的arg参数的时候，就是要使用命令行传递参数了
		int a = Integer.parseInt(args[0]);
		int b = Integer.parseInt(args[1]);
		System.out.println("结果为：" + (a + b));
	}
}
