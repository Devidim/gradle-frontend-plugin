
package com.matsuyoido.css;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.matsuyoido.LineEnd;
import com.matsuyoido.plugin.PathUtil;


/**
 * SassCompilerTest
 */
public class SassCompilerTest {

	@TempDir
	public Path tempFolder;

	private static final String SASS_FILE_DIR = PathUtil.classpathResourcePath("sassCompile");

	@Test
	public void compile_file() {
		final File sassFile = new File(SASS_FILE_DIR, "test.scss");

		final String result = new SassCompiler(LineEnd.PLATFORM).compile(sassFile);

		assertThat(result).contains("font-size: 1px;", "@media screen and (min-width: 1000px)");
	}

	@Test
	public void compile_string() {
		final String css = String.join(System.lineSeparator(), ".test {", "    font-size: 1px;", "    p {", "        color: red;", "    }", "}");
		final String result = new SassCompiler(LineEnd.PLATFORM).compile(css);

		assertThat(result).contains(".test p {");
	}
}
