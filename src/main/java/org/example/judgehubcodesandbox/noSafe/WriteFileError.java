package org.example.judgehubcodesandbox.noSafe;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * 读取服务器文件（文件信息泄露）  这里读取yml
 *
 * @author mortal
 * @date 2024/8/17 22:15
 */
public class WriteFileError {
	public static void main(String[] args) throws Exception {
		// 获取项目的工作目录
		String userDir = System.getProperty("user.dir");
		// 注意：这里获取yml配置文件的路径（不要用第三方库，会报错）
		// 这里编译没有别的环境，用纯java自带的
		// .bat 文件是一个批处理文件（Batch File），它包含了一系列命令，这些命令会按照顺序逐行执行。
		// 你可以把 .bat 文件想象成一个自动化脚本，它能够自动化执行一系列任务。
		String filePath = userDir + File.separator + "src/main/resources/木马程序.bat";
		// 模拟命令，以后可以改成危险的命令
		String errorProgram = "java -version 2>&1";
// 使用 `Files.write` 方法将内容写入文件。如果文件不存在，它会被创建：
		Files.write(Paths.get(filePath), errorProgram.getBytes());

		// 创建 ProcessBuilder 实例
		ProcessBuilder processBuilder = new ProcessBuilder(filePath);
		processBuilder.redirectErrorStream(true); // 合并标准输出和错误输出

		try {
			// 启动进程
			Process process = processBuilder.start();

			// 等待批处理文件执行完毕
			int exitCode = process.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			if (exitCode == 0) {
				// 读取进程的输出内容
				String line;
				while ((line = reader.readLine()) != null) {
					System.out.println(line);
				}
				System.out.println("Batch file executed successfully.");
			} else {
				System.err.println("Batch file execution failed with exit code: " + exitCode);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Exception occurred while executing the batch file.");
		}


		System.out.println("你个笨蛋，被我写入木马惹");
	}
}
