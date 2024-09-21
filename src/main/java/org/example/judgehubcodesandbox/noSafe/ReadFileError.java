package org.example.judgehubcodesandbox.noSafe;

import cn.hutool.core.io.FileUtil;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 *  读取服务器文件（文件信息泄露）  这里读取yml
 * @author mortal
 * @date 2024/8/17 22:15
 */
public class ReadFileError {
	public static void main(String[] args) throws Exception{
		// 获取项目的工作目录
		String userDir = System.getProperty("user.dir");
		// 注意：这里获取yml配置文件的路径（不要用第三方库，会报错）
		// 这里编译没有别的环境，用纯java自带的
		String ymlFilePath = userDir + File.separator + "src/main/resources/application.yml";
		// 读取文件中的每一行
		List<String> allLines = Files.readAllLines(Paths.get(ymlFilePath), StandardCharsets.UTF_8);
		// 用join方法，将集合 / 数组变成字符串，然后用指定的分隔符将元素进行分割 成 的一个字符串
		String ymlStr = String.join("\n", allLines);
		System.out.println(ymlStr);
	}
}
