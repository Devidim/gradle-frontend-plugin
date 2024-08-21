
package com.matsuyoido.plugin.frontend.task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.InputChanges;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.matsuyoido.js.JsMinifyCompiler;
import com.matsuyoido.plugin.frontend.extension.JavaScriptExtension;
import com.matsuyoido.plugin.frontend.extension.RootExtension;


/**
 * JsMergeTask
 */
@SuppressWarnings("javadoc")
public class JsMergeTask extends DefaultTask {

	private final boolean					continueIfErrorExist;
	private final List<JavaScriptExtension>	settings;

	@Inject
	public JsMergeTask() {
		final RootExtension extension = getProject().getExtensions().getByType(RootExtension.class);
		continueIfErrorExist = extension.getSkipError();
		settings = extension.getJSSetting();
	}

	@TaskAction
	public void mergeJavascript(final InputChanges inputChanges) {
		final MergeCompile compiler = new MergeCompile();
		settings.forEach(setting -> {
			compiler.execute(setting.getInputDirectory(), setting.getOutputDirectory());
		});
	}

	private class MergeCompile extends Compiler {

		private final ObjectMapper	   objectMapper;
		private final JsMinifyCompiler compiler;

		public MergeCompile() {
			super(".min.js", "glob:*.js.map", null, continueIfErrorExist);
			compiler = new JsMinifyCompiler(null);
			objectMapper = new ObjectMapper();
		}

		@Override
		protected String compile(final Path filePath) {
			try {
				final JsonNode jsMapper = objectMapper.readTree(filePath.toFile());
				final List<File> sourcesList = new ArrayList<>();
				jsMapper.get("sources").elements()
				        .forEachRemaining(src -> sourcesList.add(filePath.getParent().resolve(src.asText()).normalize().toFile()));
				return compiler.compile(sourcesList);
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		protected Path convetToOutputPath(final Path inputRootPath, final Path outputRootPath, final Path inputFilePath) {
			final String relativePath = inputFilePath.toString().substring(inputRootPath.toString().length() + 1);
			final String filePath = relativePath.substring(0, relativePath.lastIndexOf(".js.map")) + outputExtension;
			return outputRootPath.resolve(filePath);
		}
	}
}
