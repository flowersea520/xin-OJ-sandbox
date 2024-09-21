package org.example.judgehubcodesandbox.security;

import cn.hutool.core.io.FileUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author mortal
 * @date 2024/8/18 16:33
 */
@SpringBootTest
class MySecurityManagerTest {

	@Test
	void testPermission() {
		// 将Java的 安全管理器设置为我们自定义的
		System.setSecurityManager(new MySecurityManager());
		// 这里我们进行读权限，看看是否能被 检测出来（检查出来我们在读，就会拒绝）
//		List<String> list = FileUtil.readLines("C:\\Users\\lxc\\IdeaProjects\\planetProject\\judgehub-code-sandbox\\src\\main\\resources\\testCode\\noSafe\\ReadFileError.java", StandardCharsets.UTF_8);
		File file = FileUtil.writeString("AAA", "C:\\Users\\lxc\\IdeaProjects\\planetProject\\judgehub-code-sandbox\\src\\main\\resources\\AAA.txt", StandardCharsets.UTF_8);
//		System.out.println(FileUtil.readLines(file, StandardCharsets.UTF_8));

	}

}