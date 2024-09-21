package org.example.judgehubcodesandbox.model;

import lombok.Data;

/**
 *  判题信息
 * @author mortal
 * @date 2024/8/10 1:07
 */
@Data
public class JudgeInfo {

	/**
	 *  程序执行信息
	 */
	private String message;
	/**
	 *  程序执行时间
	 */
	private Long time;
	/**
	 *  程序执行所用到的内存
	 */
	private Long memory;
}
