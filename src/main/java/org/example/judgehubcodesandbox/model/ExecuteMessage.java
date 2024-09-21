package org.example.judgehubcodesandbox.model;

import lombok.Data;

/**
 * 进程执行信息
 */
@Data
public class ExecuteMessage {

    /**
     *  命令行回车后返回的 状态码
     */
    private Integer exitValue;

    /**
     * 根据传入不同的进程（例如编译java进程 或者是 执行class文件的进程），存入对应的输出信息
     */
    private String message;
    /**
     *  存入对应的错误的输出信息
     */

    private String errorMessage;

    /**
     *  执行时间
     */
    private Long time;

    /**
     *  执行内存
     */
    private Long memory;
}
