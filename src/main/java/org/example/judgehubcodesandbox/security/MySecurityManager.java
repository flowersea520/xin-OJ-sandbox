package org.example.judgehubcodesandbox.security;

/**
 * 默认安全管理器
 */
public class MySecurityManager extends SecurityManager {


	// 检查所有的权限
	// checkPermission 方法是自动触发的。只要你的代码在安全管理器 (SecurityManager) 设置之后，
	// 尝试执行需要权限检查的操作，JVM 就会自动调用 checkPermission 方法进行权限验证。
//	@Override
//	public void checkPermission(Permission perm) {
//		throw new SecurityException("权限异常：" + perm.toString());
//	}

	/**
	 * 检查指向权限
	 *
	 * @param cmd the specified system command.
	 */
	@Override
	public void checkExec(String cmd) {
		throw new SecurityException("权限异常：" + cmd);
	}

	/**
	 * 检查读权限（这里读的范围太大了，总是抛异常，所以我们还是注释掉）
	 *
	 * @param file the system-dependent file name.
	 */
//	@Override
//	public void checkRead(String file) {
//		throw new SecurityException("权限异常：" + file);
//	}

	/**
	 * 检查写权限
	 *
	 * @param file the system-dependent filename.
	 */
	@Override
	public void checkWrite(String file) {
		throw new SecurityException("权限异常：" + file);
	}

	/**
	 * 检查连接权限
	 *
	 * @param host the host name port to connect to.
	 * @param port the protocol port to connect to.
	 */
	@Override
	public void checkConnect(String host, int port) {
		throw new SecurityException("权限异常：" + host + port);
	}
}