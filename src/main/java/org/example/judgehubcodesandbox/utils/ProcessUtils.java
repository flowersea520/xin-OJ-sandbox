package org.example.judgehubcodesandbox.utils;

import cn.hutool.core.util.StrUtil;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.example.judgehubcodesandbox.model.ExecuteMessage;
import org.springframework.util.StopWatch;

/**
 * @author mortal
 * @date 2024/8/17 13:59
 */

/**
 * 进程工具类
 */
public class ProcessUtils {

	/**
	 * 执行进程并获取信息
	 *
	 * @param runProcess
	 * @param opName
	 * @return
	 */


	public static ExecuteMessage runProcessAndGetMessage(Process runProcess, String opName) {
		ExecuteMessage executeMessage = new ExecuteMessage();

		try {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();

			// 等待进程执行完成
			int exitValue = runProcess.waitFor();
			executeMessage.setExitValue(exitValue);

			// 分批获取进程的正常输出
			try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()))) {
				List<String> outputStrList = new ArrayList<>();
				String compileOutputLine;
				while ((compileOutputLine = bufferedReader.readLine()) != null) {
					outputStrList.add(compileOutputLine);
				}
				executeMessage.setMessage(String.join("\n", outputStrList));
			}

			if (exitValue != 0) {
				// 分批获取进程的错误输出
				try (BufferedReader errorBufferedReader = new BufferedReader(new InputStreamReader(runProcess.getErrorStream()))) {
					List<String> errorOutputStrList = new ArrayList<>();
					String errorCompileOutputLine;
					while ((errorCompileOutputLine = errorBufferedReader.readLine()) != null) {
						errorOutputStrList.add(errorCompileOutputLine);
					}
					executeMessage.setErrorMessage(String.join("\n", errorOutputStrList));
				}

				System.out.println(opName + "失败，错误码： " + exitValue);
			} else {
				System.out.println(opName + "成功");
			}

			stopWatch.stop();
			executeMessage.setTime(stopWatch.getLastTaskTimeMillis());
		} catch (Exception e) {
			e.printStackTrace();
			executeMessage.setErrorMessage(e.getMessage());
		}

		return executeMessage;
	}


	/**
	 * 执行交互式进程并获取信息
	 *
	 * @param runProcess
	 * @param args
	 * @return
	 */
	public static ExecuteMessage runInteractProcessAndGetMessage(Process runProcess, String args) {
		ExecuteMessage executeMessage = new ExecuteMessage();

		try {
			// 向控制台输入程序
			OutputStream outputStream = runProcess.getOutputStream();
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
			String[] s = args.split(" ");
			String join = StrUtil.join("\n", s) + "\n";
			outputStreamWriter.write(join);
			// 相当于按了回车，执行输入的发送
			outputStreamWriter.flush();

			// 分批获取进程的正常输出
			InputStream inputStream = runProcess.getInputStream();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
			StringBuilder compileOutputStringBuilder = new StringBuilder();
			// 逐行读取
			String compileOutputLine;
			while ((compileOutputLine = bufferedReader.readLine()) != null) {
				compileOutputStringBuilder.append(compileOutputLine);
			}
			executeMessage.setMessage(compileOutputStringBuilder.toString());
			// 记得资源的释放，否则会卡死
			outputStreamWriter.close();
			outputStream.close();
			inputStream.close();
			runProcess.destroy();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return executeMessage;
	}
}
