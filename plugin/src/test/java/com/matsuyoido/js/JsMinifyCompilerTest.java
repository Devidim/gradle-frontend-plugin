
package com.matsuyoido.js;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.matsuyoido.plugin.PathUtil;


/**
 * JsMinifyCompilerTest
 */
public class JsMinifyCompilerTest {

	@TempDir
	public Path tempFolder;

	private static final String JS_FILE_DIR = PathUtil.classpathResourcePath("minCompile");

	@Test
	public void compile_google() {
		final File jsFile = new File(JS_FILE_DIR, "test.js");

		final String result = new JsMinifyCompiler(MinifyType.GOOGLE).compile(jsFile);

		assertThat(result).isEqualTo("'use strict';function Test(){this.name=0};");
	}

	@Test
	public void compile_yui() {
		final File jsFile = new File(JS_FILE_DIR, "test.js");

		final String result = new JsMinifyCompiler(MinifyType.YUI).compile(jsFile);

		assertThat(result).isEqualTo("function Test(){this.name=0};");
	}
}
