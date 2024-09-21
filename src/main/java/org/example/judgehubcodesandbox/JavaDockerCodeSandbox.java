package org.example.judgehubcodesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.dfa.WordTree;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import org.example.judgehubcodesandbox.model.CodeExecutionRequest;
import org.example.judgehubcodesandbox.model.CodeExecutionResponse;
import org.example.judgehubcodesandbox.model.ExecuteMessage;
import org.example.judgehubcodesandbox.model.JudgeInfo;
import org.example.judgehubcodesandbox.utils.ProcessUtils;

import java.io.File;
import java.io.IOException;
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
public class JavaDockerCodeSandbox implements CodeSandBox {

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
    private static final String SECURITY_MANAGER_PATH = "/home/flowersea/code/src/main/resources/security";

    // 安全管理类的 类名
    private static final String SECURITY_MANAGER_CLASS_NAME = "MySecurityManager";

    // 标记文件的路径，表示初始化是否完成（没有指定目录，默认创建在项目的根目录）
    // 项目的根目录是指项目文件夹的顶级目录，而不是 src 目录。
    private static final String INIT_FILE_PATH = "init_done.txt";


    // 定义一个初始化的常量，随着类加载被赋值
    public static final WordTree WORD_TREE;

    static {
        WORD_TREE = new WordTree();
        WORD_TREE.addWords(BLACK_LIST);
    }

    public static void main(String[] args) {
        JavaDockerCodeSandbox javaNativeCodeSandbox = new JavaDockerCodeSandbox();
        // 这里测试 读取 resource目录下的 SleepError.java 文件，然后在代码沙箱中运行看看返回什么
        CodeExecutionRequest codeExecutionRequest = new CodeExecutionRequest();
        codeExecutionRequest.setInputList(Arrays.asList("1 2", "3 4"));
        // 这个code代码（代码肯定存字符串中），我们从 resource目录下的 SleepError.java 文件中获取
        String code = ResourceUtil.readUtf8Str("testCode/simpleCompute/Main.java");

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
        // 注意：这个File.separator 通用的常量在linux上也适合用（会随着操作系统改变而改变）
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
            String runCmd = String.format("java -Dfile.encoding=UTF-8 -cp %s Main %s", userCodeParentPath, inputArgs);
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
        // 获取默认的 Docker Client 实例，用于操作 Docker
        DockerClient dockerClient = DockerClientBuilder.getInstance().build();
        // 定义要拉取的镜像名
        String image = "openjdk:8-jdk-alpine";


        // 检查是否已经初始化过（即是否已经拉取过镜像）
//        if (!isFirstInit()) {
//            // 创建拉取镜像的命令
//            PullImageCmd pullImageCmd = dockerClient.pullImageCmd(image);
//            // 回调类，用于处理拉取镜像的结果
//            PullImageResultCallback pullImageResultCallback = new PullImageResultCallback() {
//                @Override
//                public void onNext(PullResponseItem item) {
//                    // 打印镜像下载状态
//                    System.out.println("下载镜像：" + item.getStatus());
//                    super.onNext(item);
//                }
//            };
//            try {
//                // 执行拉取镜像命令，并等待完成
//                pullImageCmd.exec(pullImageResultCallback).awaitCompletion();
//                // 拉取镜像完成后打印完成消息
//                System.out.println("下载完成");
//                // 标记初始化完成（创建了一个文件，作为标记）
//                markAsInitialized();
//            } catch (InterruptedException e) {
//                // 处理拉取镜像过程中的中断异常
//                throw new RuntimeException(e);
//            }
//        } else {
//            // 如果已经初始化过，则打印镜像已存在的消息
//            System.out.println("镜像已存在，无需重新下载。");
//        }


        // 基于指定的镜像创建一个新的 Docker 容器。
        CreateContainerCmd containerCmd = dockerClient.createContainerCmd(image);
        // 创建 Docker 容器的配置类，用于指定容器如何与主机系统进行交互
        // 可以定义容器的一些配置（例如内存，cpu）
        HostConfig hostConfig = new HostConfig();
        // 将主机系统中的文件或目录挂载到 Docker 容器中。
        // 类似于将主机的一个文件夹或文件“映射”到容器内部。
        // 这里就是将 我们的code用户代码的目录（就是文件的上一层），挂载到linux虚拟机的 /app目录下
        hostConfig.setBinds(new Bind(userCodeParentPath, new Volume("/app")));
        hostConfig.withCpuCount(2L);
        // 这里的单位默认都是字节, 我们这里设置docker容器的内存为 200MB
        hostConfig.withMemory(100 * 1024 * 1024L);
        //配置该容器在启动时执行一个简单的命令 (echo "Hello Docker"）。
        //发送请求到 Docker 守护进程创建这个容器
        // （在启动容器时使用了 tail -f /dev/null 命令。这个命令会让容器保持运行但不会产生任何输出，所以日志流中不会有任何数据）
        CreateContainerResponse containerResponse = containerCmd
                .withHostConfig(hostConfig)
                // 允许将标准输入（stdin）流附加到容器。这意味着你可以向容器的标准输入流发送数据。
                .withAttachStdin(true)
                // 允许将标准错误（stderr）流附加到容器。这意味着你可以从容器中读取错误信息。
                .withAttachStderr(true)
                // 允许将标准输出（stdout）流附加到容器。这意味着你可以从容器中读取标准输出信息。
                .withAttachStdout(true)
//        启用 TTY（伪终端）。这通常用于交互式模式，模拟终端会话。
                .withTty(true)
                .exec();
        System.out.println(containerResponse);
        //打印创建容器的结果（通常是容器 ID），以便确认容器已成功创建。
        String containerId = containerResponse.getId();
        // 启动容器
        dockerClient.startContainerCmd(containerId).exec();

        CodeExecutionResponse codeExecutionResponse = new CodeExecutionResponse();
        return codeExecutionResponse;
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
