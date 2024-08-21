
package com.matsuyoido.css;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.matsuyoido.LineEnd;
import com.matsuyoido.plugin.PathUtil;


/**
 * CssMinifyCompilerTest
 */
public class CssMinifyCompilerTest {

	@TempDir
	public Path tempFolder;

	private static final String CSS_FILE_DIR = PathUtil.classpathResourcePath("minCompile");

	@Test
	public void compile_simple() {
		final File cssFile = new File(CSS_FILE_DIR, "test.css");

		final String result = new CssMinifyCompiler(LineEnd.PLATFORM, MinifyType.SIMPLE).compile(cssFile);

		assertThat(result).isEqualTo("@charset \"UTF-8\";p{font-size:1px}a{display:flex}");
	}

	@Test
	public void compile_yui() {
		final File cssFile = new File(CSS_FILE_DIR, "test.css");

		final String result = new CssMinifyCompiler(LineEnd.PLATFORM, MinifyType.YUI).compile(cssFile);

		assertThat(result).isEqualTo("p{font-size:1px}a{display:flex}");
	}
}
