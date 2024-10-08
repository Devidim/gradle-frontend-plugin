
package com.matsuyoido.plugin.frontend.task;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
// import java.io.File;
// import java.util.ArrayList;
import java.util.Arrays;
// import java.util.Collections;
// import java.util.List;

import org.gradle.testfixtures.ProjectBuilder;
import org.gradle.work.InputChanges;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.matsuyoido.LineEnd;
// import com.matsuyoido.caniuse.SupportData;
// import com.matsuyoido.caniuse.SupportLevel;
// import com.matsuyoido.caniuse.SupportStatus;
// import com.matsuyoido.caniuse.VersionPrefixer;
// import com.matsuyoido.plugin.LineEnd;
import com.matsuyoido.plugin.PathUtil;
import com.matsuyoido.plugin.frontend.extension.CssExtension;


/**
 * CssMinifyTaskTest
 */
@Disabled("TaskのテストはPlugin の結合テストで担保することとする")
public class CssMinifyTaskTest {

	@TempDir
	public Path tempFolder;

	private static final String CSS_FILE_DIR = PathUtil.classpathResourcePath("minCompile");

	private final InputChanges inputChanges	= new MockInputChanges();
	private CssMinifyTask	   task;

	@BeforeEach
	public void setup() {
		final CssExtension extension = new CssExtension();
		extension.setInDir(new File(CSS_FILE_DIR));
		extension.setAddPrefixer(true);
		extension.setOutDir(tempFolder.toFile());

		task = ProjectBuilder.builder().build().getTasks().create("test", CssMinifyTask.class, LineEnd.WINDOWS, Arrays.asList(extension), null);
	}

	@Test
	public void minifyCss() throws IOException {
		task.minifyCss(inputChanges);

		assertThat(tempFolder.toFile().listFiles()).allSatisfy(file -> {
			if (file.getName().equals("nest")) {
				assertThat(file.listFiles()).allSatisfy(nestFile -> {
					assertThat(nestFile).isFile().satisfies(f -> assertThat(f.getName()).endsWith(".min.css")).as("nest file content check")
					        .hasContent("p{font-size:1rem;color:red}");
				}).hasSize(1);
			} else {
				assertThat(file).isFile().satisfies(f -> assertThat(f.getName()).endsWith(".min.css")).as("flat file content check")
				        .hasContent("p{font-size:1px}a{display:flex}");
			}
		}).hasSize(2);
	}

	@Test
	public void minifyCss_andMinify() {
		// task.setPrefixer(Collections.singletonList(flexSupport()))
		// .minifyCss(inputs);

		assertThat(tempFolder.toFile().listFiles()).allSatisfy(file -> {
			if (file.getName().equals("nest")) {
				assertThat(file.listFiles()).allSatisfy(nestFile -> {
					assertThat(nestFile).isFile().satisfies(f -> assertThat(f.getName()).endsWith(".min.css")).as("nest file content check")
					        .hasContent("@charset \"UTF-8\";p{color:red;font-size:1rem}");
				}).hasSize(1);
			} else {
				assertThat(file).isFile().satisfies(f -> assertThat(f.getName()).endsWith(".min.css")).as("flat file content check")
				        .hasContent("@charset \"UTF-8\";p{font-size:1px}a{display:flex;display:-webkit-flex}");
			}
		}).hasSize(2);
	}

	// private SupportData flexSupport() {
	// SupportStatus chromeStatus = new SupportStatus("chrome");
	// chromeStatus.addSupportVersion(new VersionPrefixer("10", "webkit"), SupportLevel.ENABLE_WITH_PREFIX);

	// List<SupportStatus> supports = new ArrayList<>();
	// supports.add(chromeStatus);

	// SupportData support = new SupportData();
	// support.setKey("flexbox");
	// support.setSupports(supports);
	// return support;
	// }
}
