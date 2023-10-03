package com.example.controller.error;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.ui.Model;
import org.springframework.core.env.Environment;

import java.io.PrintWriter;
import java.io.StringWriter;

@ControllerAdvice
public class GlobalExceptionHandler {

	@Autowired
	private ErrorAttributes errorAttributes;

	@Autowired
	private Environment environment;

	@ExceptionHandler(NotFoundException.class) // カスタム例外クラスを作成して404エラーをハンドリング
	public String handleNotFound() {
		return "error/404"; // 404エラーページのパスを返す
	}

	@ExceptionHandler(Exception.class)
	public String handle500Error(Model model, WebRequest request) {
		// server.error.include-stacktrace プロパティの値を確認
		String includeStacktrace = environment.getProperty("server.error.include-stacktrace");

		// スタックトレースを取得
		if ("always".equalsIgnoreCase(includeStacktrace)) {
			Throwable error = errorAttributes.getError(request);
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			error.printStackTrace(pw);
			String stackTrace = sw.toString();

			// スタックトレースをテンプレートに渡す
			model.addAttribute("stackTrace", stackTrace);
		}

		return "error/500"; // 500エラーページのパスを返す
	}

	@ResponseStatus(HttpStatus.NOT_FOUND)
	public static class NotFoundException extends RuntimeException {
		public NotFoundException() {
			super("リソースが見つかりません。");
		}
	}
}
