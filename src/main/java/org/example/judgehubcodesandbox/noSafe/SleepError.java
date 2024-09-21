package org.example.judgehubcodesandbox.noSafe;

public class SleepError {
	public static void main(String[] args) throws InterruptedException {
		// 睡眠 1h
		long time = 1000 * 60 * 60;
		Thread.sleep(time);
		System.out.println("睡完了");
	}
}
