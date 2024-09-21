package org.example.judgehubcodesandbox.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author mortal
 * @date 2024/8/16 17:48
 */
@RestController
@ResponseBody
public class MainController {

	@GetMapping("/")
	public String sayHello() {
		return "hello";
	}
}
