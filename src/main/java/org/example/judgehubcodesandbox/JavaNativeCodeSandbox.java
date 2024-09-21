package org.example.judgehubcodesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.dfa.FoundWord;
import cn.hutool.dfa.WordTree;
import org.example.judgehubcodesandbox.model.CodeExecutionRequest;
import org.example.judgehubcodesandbox.model.CodeExecutionResponse;
import org.example.judgehubcodesandbox.model.ExecuteMessage;
import org.example.judgehubcodesandbox.model.JudgeInfo;
import org.example.judgehubcodesandbox.security.MySecurityManager;
import org.example.judgehubcodesandbox.utils.ProcessUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 本地的Java代码沙箱
 *
 * @author mortal
 * @date 2024/8/16 21:47
 */
public class JavaNativeCodeSandbox implements CodeSandBox {

	// 存放所有Java代码的目录名
	private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";

	// Java文件名（这里就是存放 未编译的） -- 写在项目工作目录的 tmpCode目录下的都是以 Main.java命令的文件
	// 不同的 文件夹名 包裹着（UUID），通过下面这个命令将代码写入到这个文件路径下面了
	// FileUtil.writeString(code, userCodeFilePath, StandardCharsets.UTF_8);
	private static final String GLOGAL_JAVA_CLASS_NAME = "RunFileError.java";

	// 自定义一个超时时间，如果过了超时时间，直接杀死进程（这里设置为5秒）
	public static final Long TIME_OUT = 5000L;

	// 弄一个黑名单
	public static final List<String> BLACK_LIST = Arrays.asList("Files", "exec");

	// 安全管理类的路径
	private static final String SECURITY_MANAGER_PATH = "C:\\Users\\lxc\\IdeaProjects\\planetProject\\judgehub-code-sandbox\\src\\main\\resources\\security";

	// 安全管理类的 类名
	private static final String SECURITY_MANAGER_CLASS_NAME = "MySecurityManager";


	// 定义一个初始化的常量，随着类加载被赋值
	public static final WordTree WORD_TREE;

	static {
		WORD_TREE = new WordTree();
		WORD_TREE.addWords(BLACK_LIST);
	}

	public static void main(String[] args) {
		JavaNativeCodeSandbox javaNativeCodeSandbox = new JavaNativeCodeSandbox();
		// 这里测试 读取 resource目录下的 SleepError.java 文件，然后在代码沙箱中运行看看返回什么
		CodeExecutionRequest codeExecutionRequest = new CodeExecutionRequest();
		codeExecutionRequest.setInputList(Arrays.asList("1 2", "3 4"));
		// 这个code代码（代码肯定存字符串中），我们从 resource目录下的 SleepError.java 文件中获取
		String code = ResourceUtil.readUtf8Str("testCode/noSafe/RunFileError.java");

//		String code = ResourceUtil.readUtf8Str("testCode/noSafe/WriteFileError.java");
		codeExecutionRequest.setCode(code);
		codeExecutionRequest.setLanguage("java");
		CodeExecutionResponse codeExecutionResponse = javaNativeCodeSandbox.executeCode(codeExecutionRequest);
		System.out.println(codeExecutionResponse);

	}

	@Override
	public CodeExecutionResponse executeCode(CodeExecutionRequest codeExecutionRequest) {
		// 设置自定义的 SecurityManager 安全管理器（这个方法允许你替换 JVM 的默认安全管理器）


		List<String> inputList = codeExecutionRequest.getInputList();
		String code = codeExecutionRequest.getCode();
		String language = codeExecutionRequest.getLanguage();

		// 在文件编译前就判断code用户上传的代码是否在黑名单中
		// 这里用hutool的关键词树
//		FoundWord foundWord = WORD_TREE.matchWord(code);
//		if (foundWord != null) {
//			System.out.println("包含禁止词" + foundWord.getFoundWord());
//			return null;
//		}


//        1. 把用户的代码保存为文件
		// todo 例如这行代码就需要权限，然后就会触发
		String userDir = System.getProperty("user.dir");
		String globalCodePathName = userDir + File.separator + GLOBAL_CODE_DIR_NAME;
		// 判断全局代码目录是否存在，没有则新建
		if (!FileUtil.exist(globalCodePathName)) {
			FileUtil.mkdir(globalCodePathName);
		}

		// 把用户的代码隔离存放
		// 例如：这个就是tmpCode目录的完整路径  xxx/tmpCode
		String userCodeParentPath = globalCodePathName + File.separator + UUID.randomUUID();
		// 获取用户输入的code，存入的文件路径（都是起名字Main.java)
		String userCodeFilePath = userCodeParentPath + File.separator + GLOGAL_JAVA_CLASS_NAME;
		// 有了文件路径，就直接创建就好了
		File userCodeFile = FileUtil.writeString(code, userCodeFilePath, StandardCharsets.UTF_8);

//        2. 编译代码，得到 class 文件
		String compileCmd = String.format("javac -encoding utf-8 %s", userCodeFile.getAbsolutePath());
		try {
			Process compileProcess = Runtime.getRuntime().exec(compileCmd);
			ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(compileProcess, "编译");
			System.out.println(executeMessage);
		} catch (Exception e) {
			return getErrorResponse(e);
		}

		// 3. 执行代码，得到输出结果
		List<ExecuteMessage> executeMessageList = new ArrayList<>();
		for (String inputArgs : inputList) {
//            String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s Main %s", userCodeParentPath, inputArgs);
			// 这个Main不变，因为我们的类名都是 Main，所以就是运行这个Main类
//			 执行class文件的时候指定 jvm的参数，限制其堆内存的大小，这样就不会出现内存溢出的情况的
//			String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp  %s Main %s", userCodeParentPath, inputArgs);
// 			todo 这里在Java 执行class文件的时候就设置 自定义的安全管理器
			String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s;%s -Djava.security.manager=%s Main %s", userCodeParentPath, SECURITY_MANAGER_PATH, SECURITY_MANAGER_CLASS_NAME, inputArgs);
			try {
				// 开启了一个进程
				Process runProcess = Runtime.getRuntime().exec(runCmd);
				// todo  程序执行的时候，这里新开一个线程，睡上5秒，作为超时时间，如果5秒之后程序还没执行完，那么我就将进程杀掉
				new Thread(() -> {
					// 通过创建一个新线程，可以让主线程继续处理其他任务，而新线程负责监控时间。避免主线程阻塞
					try {
						Thread.sleep(TIME_OUT);
						System.out.println("超时了");
						// 5秒后没执行完，就杀死进程程
						runProcess.destroy();
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}).start();
				ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(runProcess, "运行");
//                ExecuteMessage executeMessage = ProcessUtils.runInteractProcessAndGetMessage(runProcess, inputArgs);
				System.out.println(executeMessage);
				executeMessageList.add(executeMessage);
			} catch (Exception e) {
				return getErrorResponse(e);
			}
		}

//        4. 收集整理输出结果
		CodeExecutionResponse executeCodeResponse = new CodeExecutionResponse();
		List<String> outputList = new ArrayList<>();
		// 取用时最大值，便于判断是否超时
		long maxTime = 0;
		for (ExecuteMessage executeMessage : executeMessageList) {
			String errorMessage = executeMessage.getErrorMessage();
			if (StrUtil.isNotBlank(errorMessage)) {
				executeCodeResponse.setMessage(errorMessage);
				// 用户提交的代码执行中存在错误
				executeCodeResponse.setStatus(3);
				break;
			}
			outputList.add(executeMessage.getMessage());
			Long time = executeMessage.getTime();
			if (time != null) {
				maxTime = Math.max(maxTime, time);
			}
		}
		// 正常运行完成
		if (outputList.size() == executeMessageList.size()) {
			executeCodeResponse.setStatus(1);
		}
		executeCodeResponse.setOutputList(outputList);
		JudgeInfo judgeInfo = new JudgeInfo();
		judgeInfo.setTime(maxTime);
		// 要借助第三方库来获取内存占用，非常麻烦，此处不做实现
//        judgeInfo.setMemory();

		executeCodeResponse.setJudgeInfo(judgeInfo);

//        5. 文件清理
		if (userCodeFile.getParentFile() != null) {
			boolean del = FileUtil.del(userCodeParentPath);
			System.out.println("删除" + (del ? "成功" : "失败"));
		}
		return executeCodeResponse;
	}

	/**
	 * 错误处理，提高程序的健壮性
	 * 获取错误相应对象（都设置为空对象，而不是让他为null）
	 * 就是为了 给try catch中的 catch用的
	 *
	 * @param e 异常类的超类
	 * @return
	 */
	private CodeExecutionResponse getErrorResponse(Throwable e) {
		CodeExecutionResponse codeExecutionResponse = new CodeExecutionResponse();
		// 自定义错误的异常相应对象
		codeExecutionResponse.setOutputList(new ArrayList<>());
		codeExecutionResponse.setMessage(e.getMessage());
		// 表示代码沙箱的错误
		codeExecutionResponse.setStatus(2);
		codeExecutionResponse.setJudgeInfo(new JudgeInfo());
		return codeExecutionResponse;

	}
}
