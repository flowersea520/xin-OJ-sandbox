package org.example.judgehubcodesandbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 代码沙箱的响应模型：确定沙箱如何返回执行结果或错误信息。
 * 包括执行输出、错误信息、状态码等。
 *
 * @author mortal
 * @date 2024/8/15 0:20
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CodeExecutionResponse {

	/**
	 * 输出信息：
	 * （用集合的好处：响应给前端一个列表，不用让前端解析json字符串）
	 */
	private List<String> outputList;

	/**
	 * 接口信息
	 */
	private String message;

	/**
	 * 执行状态
	 */

	private Integer status;

	/**
	 * 判题信息
	 */
	private JudgeInfo judgeInfo;

}
