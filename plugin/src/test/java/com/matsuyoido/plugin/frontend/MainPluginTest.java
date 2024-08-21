
package com.matsuyoido.plugin.frontend;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.matsuyoido.plugin.PathUtil;


/**
 * MainPluginTest
 */
public class MainPluginTest {

	@TempDir
	public Path projectDir;

	private File getProjectDir() throws IOException {
		return projectDir.toFile().getCanonicalFile();
	}

	private BuildResult run(final String... taskName) throws IOException {
		return GradleRunner.create().withGradleVersion("5.0").withProjectDir(getProjectDir()).withPluginClasspath().withDebug(true)
		        .withArguments(taskName).forwardOutput().build();
	}

	private void setup(final String... extension) throws IOException {
		projectDir.resolve("settings.gradle").toFile().createNewFile();
		final File buildGradle = projectDir.resolve("build.gradle").toFile();
		buildGradle.createNewFile();
		final String classpath = GradleRunner.create().withPluginClasspath().getPluginClasspath().stream()
		        .map(f -> String.format("'%s'", f.getAbsolutePath())).collect(Collectors.joining(", ")).replace("\\", "/");
		final StringBuilder buildScript = new StringBuilder();
		buildScript.append(String.join(System.lineSeparator(), "buildscript {", "  dependencies {", "    classpath files(" + classpath + ")", "  }",
		        "}", "import com.matsuyoido.plugin.frontend.MainPlugin", "apply plugin: MainPlugin")).append(System.lineSeparator());
		buildScript.append(Arrays.stream(extension).collect(Collectors.joining(System.lineSeparator())));
		final String text = buildScript.toString();

		System.out.println(text);
		// Files.writeString(buildGradle.toPath(), text);
		Files.write(buildGradle.toPath(), text.getBytes());
	}

	@Test
	public void enabledTasks_all() throws Exception {
		final Path dataJsonPath = getProjectDir().toPath().resolve("data.json");
		Files.copy(new File(PathUtil.classpathResourcePath("caniuse/data.json")).toPath(), dataJsonPath);
		setup("frontend {", "  setting {", "    lineEnding = 'windows'", "    prefixer {", "      caniuseData = file(\"$projectDir/data.json\")",
		        "      ie = ''", "      edge = ''", "      chrome = 'all'", "      firefox = ''", "      safari = ''", "      ios = ''",
		        "      android = ''", "    }", "  }", "  style {", "    scss {", "      inDir = file(\"$projectDir/src/main/sass\")",
		        "      outDir = file(\"$projectDir/src/main/resources/static/css\")", "      addPrefixer = true", "      enableMinify = true",
		        "    }", "    scss {", "      inDir = file(\"$projectDir/src/main/sass2\")",
		        "      outDir = file(\"$projectDir/src/main/resources/static/css2\")", "    }", "    css {",
		        "      inDir = file(\"$projectDir/src/main/resources/static/css\")",
		        "      outDir = file(\"$projectDir/src/main/resources/static/css\")", "      addPrefixer = true", "    }", "    css {",
		        "      inDir = file(\"$projectDir/src/main/resources/static/css1\")",
		        "      outDir = file(\"$projectDir/src/main/resources/static/css1\")", "    }", "  }", "  script {", "    js {",
		        "      inDir = file(\"$projectDir/src/main/resources/static/js\")",
		        "      outDir = file(\"$projectDir/src/main/resources/static/js\")", "      type = 'yahoo'", "    }", "    js {",
		        "      inDir = file(\"$projectDir/src/main/resources/static/js2\")",
		        "      outDir = file(\"$projectDir/src/main/resources/static/js2\")", "    }", "  }", "", "}");

		final String result = run("tasks", "--all").getOutput();
		assertThat(result).contains("sassCompile", "cssMinify", "jsMinify", "jsMerge");
	}

	@Test
	public void enabledTask_onlySass() throws IOException {
		setup("frontend {", "  style {", "    scss {", "      inDir = file(\"$projectDir/src/main/sass\")",
		        "      outDir = file(\"$projectDir/src/main/resources/static/css\")", "      addPrefixer = true", "      enableMinify = true",
		        "    }", "  }", "}");

		final String result = run("tasks", "--all").getOutput();
		assertThat(result).contains("sassCompile").doesNotContain("cssMinify", "jsMinify", "jsMerge");
	}

	@Test
	public void enabledTask_onlyCss() throws IOException {
		setup("frontend {", "  style {", "    css {", "      inDir = file(\"$projectDir/src/main/sass\")",
		        "      outDir = file(\"$projectDir/src/main/resources/static/css\")", "      addPrefixer = true", "    }", "  }", "}");

		final String result = run("tasks", "--all").getOutput();
		assertThat(result).contains("cssMinify").doesNotContain("sassCompile", "jsMinify", "jsMerge");
	}

	@Test
	public void enabledTasks_onlyJs() throws IOException {
		setup("frontend {", "  script {", "    js {", "      inDir = file(\"$projectDir/src/main/resources/static/js\")",
		        "      outDir = file(\"$projectDir/src/main/resources/static/js\")", "    }", "  }", "}");

		final String result = run("tasks", "--all").getOutput();
		assertThat(result).contains("jsMinify", "jsMerge").doesNotContain("sassCompile", "cssMinify");
	}

	@Test
	public void sassCompileTask_onlyCompile() throws IOException {
		final File sassDir = new File(getProjectDir(), "src/main/sass");
		final File cssDir = new File(getProjectDir(), "src/main/resources/static/css");

		sassDir.mkdirs();
		cssDir.mkdirs();
		final File sassFile = new File(sassDir, "child.scss");
		final File notCompileFile = new File(sassDir, "_notCompile.scss");
		sassFile.createNewFile();
		notCompileFile.createNewFile();

		setup("frontend {", "  style {", "    scss {", "      inDir = file(\"$projectDir/src/main/sass\")",
		        "      outDir = file(\"$projectDir/src/main/resources/static/css\")", "    }", "  }", "}");

		final BuildTask result = run("sassCompile").task(":sassCompile");

		assertThat(result.getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
		assertThat(cssDir.list()).hasSize(1).containsOnly("child.css");
	}

	@Test
	public void sassCompileTask_andMinify() throws IOException {
		final File sassDir = new File(getProjectDir(), "src/main/sass");
		final File cssDir = new File(getProjectDir(), "src/main/resources/static/css");

		sassDir.mkdirs();
		cssDir.mkdirs();
		final File sassFile = new File(sassDir, "child.scss");
		final File notCompileFile = new File(sassDir, "_notCompile.scss");
		sassFile.createNewFile();
		notCompileFile.createNewFile();

		setup("frontend {", "  style {", "    scss {", "      inDir = file(\"$projectDir/src/main/sass\")",
		        "      outDir = file(\"$projectDir/src/main/resources/static/css\")", "      enableMinify = true", "    }", "  }", "}");

		final BuildResult result = run("sassCompile");

		assertThat(result.task(":sassCompile").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
		assertThat(cssDir.list()).hasSize(1).containsOnly("child.min.css");
	}

	@Test
	public void sassCompileTask_andMinify_executeTwice() throws IOException {
		final File sassDir = new File(getProjectDir(), "src/main/sass");
		final File cssDir = new File(getProjectDir(), "src/main/resources/static/css");

		sassDir.mkdirs();
		cssDir.mkdirs();
		final File sassFile = new File(sassDir, "child.scss");
		final File notCompileFile = new File(sassDir, "_notCompile.scss");
		sassFile.createNewFile();
		notCompileFile.createNewFile();

		setup("frontend {", "  style {", "    scss {", "      inDir = file(\"$projectDir/src/main/sass\")",
		        "      outDir = file(\"$projectDir/src/main/resources/static/css\")", "      enableMinify = true", "    }", "  }", "}");

		BuildResult result = run("sassCompile");

		assertThat(result.task(":sassCompile").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
		assertThat(cssDir.list()).hasSize(1).containsOnly("child.min.css");
		result = run("sassCompile");

		assertThat(result.task(":sassCompile").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
		assertThat(cssDir.list()).hasSize(1).containsOnly("child.min.css");
	}

	@Test
	public void sassCompileTask_andMinify_onlyExistMinCss() throws IOException {
		final File sassDir1 = new File(getProjectDir(), "src/main/sass/custom");
		final File cssDir = new File(getProjectDir(), "src/main/resources/static/css");
		final File sassDir2 = new File(getProjectDir(), "src/main/sass/vendor");

		sassDir1.mkdirs();
		sassDir2.mkdirs();
		cssDir.mkdirs();
		final File sassFile1 = new File(sassDir1, "child.scss");
		final File notCompileFile = new File(sassDir1, "_notCompile.scss");
		final File sassFile2 = new File(sassDir2, "child2.scss");
		sassFile1.createNewFile();
		sassFile2.createNewFile();
		notCompileFile.createNewFile();

		setup("frontend {", "  style {", "    scss {", "      inDir = file(\"$projectDir/src/main/sass/custom\")",
		        "      outDir = file(\"$projectDir/src/main/resources/static/css\")", "      enableMinify = true", "    }", "    scss {",
		        "      inDir = file(\"$projectDir/src/main/sass/vendor\")", "      outDir = file(\"$projectDir/src/main/resources/static/css\")",
		        "    }", "  }", "}");

		final BuildResult result = run("sassCompile");

		assertThat(result.task(":sassCompile").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);

		assertThat(cssDir.list()).hasSize(2).containsOnly("child.min.css", "child2.css");
	}

	@Test
	public void cssMinify_onlyMinify() throws IOException {
		final File cssInDir = new File(getProjectDir(), "src/main/resources/static/css");
		final File cssOutDir = new File(getProjectDir(), "build/resources/static/css");

		cssInDir.mkdirs();
		cssOutDir.mkdirs();
		final File cssFile = new File(cssInDir, "child.css");
		final File notCompileFile = new File(cssInDir, "notCompile.min.css");
		cssFile.createNewFile();
		notCompileFile.createNewFile();

		setup("frontend {", "  style {", "    css {", "      inDir = file(\"$projectDir/src/main/resources/static/css\")",
		        "      outDir = file(\"$projectDir/build/resources/static/css\")", "    }", "  }", "}");

		final BuildResult result = run("cssMinify");
		assertThat(result.task(":cssMinify").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
		assertThat(cssOutDir.list()).hasSize(1).containsOnly("child.min.css");
	}

	@Test
	public void cssMinify_andPrefixer() throws IOException {
		final File cssInDir = new File(getProjectDir(), "src/main/resources/static/css");
		final File cssOutDir = new File(getProjectDir(), "build/resources/static/css");
		final Path dataJsonPath = getProjectDir().toPath().resolve("data.json");

		// Files.copy(Path.of(PathUtil.classpathResourcePath("caniuse/data.json")), dataJsonPath);
		Files.copy(new File(PathUtil.classpathResourcePath("caniuse/data.json")).toPath(), dataJsonPath);

		cssInDir.mkdirs();
		cssOutDir.mkdirs();
		final File notCompileFile = new File(cssInDir, "notCompile.min.css");
		notCompileFile.createNewFile();

		// @SuppressWarnings("unused")
		// Path cssFile = Files.writeString(cssInDir.toPath().resolve("child.css"),
		// "a { display: flex; }",
		// StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
		Files.write(cssInDir.toPath().resolve("child.css"), "a { display: flex; }".getBytes(), StandardOpenOption.CREATE_NEW);

		setup("frontend {", "  setting {", "    prefixer {", "      caniuseData = file(\"$projectDir/data.json\")", "      ie = ''",
		        "      edge = ''", "      chrome = 'all'", "      firefox = ''", "      safari = ''", "      ios = ''", "      android = ''", "    }",
		        "  }", "  style {", "    css {", "      inDir = file(\"$projectDir/src/main/resources/static/css\")",
		        "      outDir = file(\"$projectDir/build/resources/static/css\")", "      addPrefixer = true", "    }", "  }", "}");

		final BuildResult result = run("cssMinify", "--stacktrace");
		assertThat(result.task(":cssMinify").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
		// String cssValue = Files.readString(cssOutDir.listFiles()[0].toPath());
		final String cssValue = Files.readAllLines(cssOutDir.listFiles()[0].toPath()).stream().collect(Collectors.joining(System.lineSeparator()));
		assertThat(cssValue).isEqualTo("@charset \"UTF-8\";a{display:flex;display:-webkit-flex}");

		assertThat(cssOutDir.list()).hasSize(1).containsOnly("child.min.css");
	}

	@Test
	public void jsMinify() throws IOException {
		final File jsInDir = new File(getProjectDir(), "src/main/resources/static/js");
		final File jsOutDir = new File(getProjectDir(), "build/resources/static/js");

		jsInDir.mkdirs();
		jsOutDir.mkdirs();
		final File notCompileFile = new File(jsInDir, "compiled.min.js");
		notCompileFile.createNewFile();
		Files.write(jsInDir.toPath().resolve("child.js"), "function hoge() {}".getBytes(), StandardOpenOption.CREATE_NEW);

		setup("frontend {", "  script {", "    js {", "      inDir = file(\"$projectDir/src/main/resources/static/js\")",
		        "      outDir = file(\"$projectDir/build/resources/static/js\")", "      type = \"yahoo\"", "    }", "  }", "}");

		final BuildResult result = run("jsMinify");
		assertThat(result.task(":jsMinify").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
		assertThat(jsOutDir.list()).hasSize(1).containsOnly("child.min.js");
	}

	@Test
	public void jsMerge() throws IOException {
		final File jsInDir = new File(getProjectDir(), "src/main/js");
		final File jsOutDir = new File(getProjectDir(), "build/resources/static/js");

		new File(jsInDir, "inner").mkdirs();
		jsOutDir.mkdirs();
		Files.write(jsInDir.toPath().resolve("child.js"), "function hoge() {}".getBytes(), StandardOpenOption.CREATE_NEW);
		Files.write(jsInDir.toPath().resolve("inner/child.js"), "function fuga() {}".getBytes(), StandardOpenOption.CREATE_NEW);
		Files.write(jsInDir.toPath().resolve("test.js.map"), "{ \"sources\": [\"child.js\", \"./inner/child.js\"] }".getBytes(),
		        StandardOpenOption.CREATE_NEW);

		setup("frontend {", "  script {", "    js {", "      inDir = file(\"$projectDir/src/main/js\")",
		        "      outDir = file(\"$projectDir/build/resources/static/js\")", "    }", "  }", "}");

		final BuildResult result = run("jsMerge");
		assertThat(result.task(":jsMerge").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
		assertThat(jsOutDir.list()).hasSize(1).containsOnly("test.min.js");
	}

	@Test
	public void jsMinify_fileNameEndContainsMin() throws Exception {
		final File jsInDir = new File(getProjectDir(), "src/main/resources/static/js");
		final File jsOutDir = new File(getProjectDir(), "build/resources/static/js");

		jsInDir.mkdirs();
		jsOutDir.mkdirs();
		final File notCompileFile = new File(jsInDir, "compiled.min.js");
		notCompileFile.createNewFile();
		Files.write(jsInDir.toPath().resolve("childmin.js"), "function hoge() {}".getBytes(), StandardOpenOption.CREATE_NEW);
		Files.write(jsInDir.toPath().resolve("child.js"), "function hoge() {}".getBytes(), StandardOpenOption.CREATE_NEW);
		Files.write(jsInDir.toPath().resolve("childm.js"), "function hoge() {}".getBytes(), StandardOpenOption.CREATE_NEW);
		jsInDir.toPath().resolve("hoge").toFile().mkdirs();
		Files.write(jsInDir.toPath().resolve("hoge/childmin.js"), "function hoge() {}".getBytes(), StandardOpenOption.CREATE_NEW);
		Files.write(jsInDir.toPath().resolve("hoge/childm.min.js"), "function hoge() {}".getBytes(), StandardOpenOption.CREATE_NEW);

		setup("frontend {", "  script {", "    js {", "      inDir = file(\"$projectDir/src/main/resources/static/js\")",
		        "      outDir = file(\"$projectDir/build/resources/static/js\")", "      type = \"yahoo\"", "    }", "  }", "}");

		final BuildResult result = run("jsMinify");
		assertThat(result.task(":jsMinify").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
		assertThat(jsOutDir.list()).hasSize(4).containsOnly("childmin.min.js", "child.min.js", "childm.min.js", "hoge");
		assertThat(jsOutDir.toPath().resolve("hoge").toFile().list()).hasSize(1).containsOnly("childmin.min.js");
	}

}
