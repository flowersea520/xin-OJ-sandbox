package org.example.judgehubcodesandbox;


import org.example.judgehubcodesandbox.model.CodeExecutionRequest;
import org.example.judgehubcodesandbox.model.CodeExecutionResponse;

/**
 * @author mortal
 * @date 2024/8/15 0:23
 */
public interface CodeSandBox {
	/**
	 * 代码沙箱执行代码
	 *
	 * @param codeExecutionRequest
	 * @return
	 */
	CodeExecutionResponse executeCode(CodeExecutionRequest codeExecutionRequest);
}
