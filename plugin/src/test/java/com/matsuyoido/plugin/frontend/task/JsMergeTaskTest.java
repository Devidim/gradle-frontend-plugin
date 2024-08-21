
package com.matsuyoido.plugin.frontend.task;

import static org.assertj.core.api.Assertions.assertThat;
// import java.io.File;

import java.nio.file.Path;

import org.gradle.testfixtures.ProjectBuilder;
import org.gradle.work.InputChanges;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;


/**
 * JsMergeTaskTest
 */
@Disabled("TaskのテストはPlugin の結合テストで担保することとする")
public class JsMergeTaskTest {

	@TempDir
	public Path tempFolder;

	// private static final String SOURCE_FILE_DIR = PathUtil.classpathResourcePath("mergeCompile");

	private final InputChanges inputChanges	= new MockInputChanges();
	private final JsMergeTask  task			= ProjectBuilder.builder().build().getTasks().create("test", JsMergeTask.class);

	@BeforeEach
	public void setup() {
		// task.setJsMapDirectory(new File(SOURCE_FILE_DIR))
		// .setOutputFileDirectory(tempFolder.getRoot());
	}

	@Test
	public void mergeJavascript() {
		task.mergeJavascript(inputChanges);

		assertThat(tempFolder.toFile().listFiles()).allSatisfy(file -> {
			assertThat(file).isFile().hasName("test.min.js").hasContent("'use strict';function Test(){this.name=0};const test=\"\";");
		});
	}
}
