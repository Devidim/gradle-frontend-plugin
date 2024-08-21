
package com.matsuyoido.plugin.frontend.task;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.matsuyoido.plugin.PathUtil;


/**
 * CompilerTest
 */
@Disabled("TaskのテストはPlugin の結合テストで担保することとする")
public class CompilerTest {

	@TempDir
	public Path tempFolder;

	@Test
	public void getTargets_startsWith_Exclude() throws IOException {
		// case
		final Compiler compiler = new Compiler("css", "glob:[!_]*.scss", null, false) {
			@Override
			protected String compile(final Path filePath) {
				return null;
			}
		};
		final String directory = PathUtil.classpathResourcePath("sassCompile");

		// execute
		final Set<Path> result = compiler.getTargets(new File(directory));

		final int expectFileCount = 2;
		assertThat(result).hasSize(expectFileCount);
	}

	@Test
	public void getTargets_minFileExclude() throws IOException {
		final Compiler compiler = new Compiler("css", "glob:*.css", Collections.singleton("glob:*.min.css"), false) {
			@Override
			protected String compile(final Path filePath) {
				return null;
			}
		};
		final String directory = PathUtil.classpathResourcePath("minCompile");

		// execute
		final Set<Path> result = compiler.getTargets(new File(directory));

		final int expectFileCount = 2;
		assertThat(result).hasSize(expectFileCount);
	}

	@Test
	public void convertToOutputPath_nest() {
		// case
		final String rootPath = "src/main/hogehoge";
		final Path inputRootPath = new File(rootPath).toPath();// Path.of(rootPath);
		final Path outputRootPath = new File("src/test/hogehoge").toPath();// Path.of("src/test/hogehoge");
		final Path inputFilePath = new File(rootPath, "test/test.tmp").toPath();// Path.of(rootPath, "test/test.tmp");

		final Compiler compiler = new TestCompiler();

		// execute
		final Path result = compiler.convetToOutputPath(inputRootPath, outputRootPath, inputFilePath);

		final Path expect = new File("src/test/hogehoge", "test/test.tmp").toPath();// Path.of("src/test/hogehoge", "test/test.tmp");
		assertThat(result.toString()).isEqualTo(expect.toString());
	}

	@Test
	public void outputFile() throws IOException {
		// case
		final File outputDirectory = tempFolder.resolve("compiledFolder").toFile();
		outputDirectory.mkdir();
		final File outputFile = File.createTempFile("prefix", ".tmp", outputDirectory);
		final String writeContent = "content";

		final Compiler compiler = new TestCompiler();

		// execute
		compiler.outputFile(outputFile, writeContent);

		assertThat(outputFile).hasContent(writeContent);
	}

	private class TestCompiler extends Compiler {

		public TestCompiler() {
			super("tmp", "", null, false);
		}

		@Override
		protected String compile(final Path filePath) {
			return null;
		}

	}
}
