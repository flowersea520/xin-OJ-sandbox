package org.example.judgehubcodesandbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 代码沙箱的请求模型: 确定客户端如何向沙箱发送代码及其相关信息。
 * 包括代码内容、编程语言、可能的输入参数（一般用集合List）等。
 *
 * @author mortal
 * @date 2024/8/15 0:20
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CodeExecutionRequest {

	/**
	 * 输入信息 （用集合的好处：接收前端传过来的一个列表数组，我们后端不用将字符串解析成Java对象了）
	 */
	private List<String> inputList;

	/**
	 * 请求的代码
	 */
	private String code;

	/**
	 * 代码的语言
	 */

	private String language;

}
