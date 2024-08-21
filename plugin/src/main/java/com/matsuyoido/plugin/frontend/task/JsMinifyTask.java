
package com.matsuyoido.plugin.frontend.task;

import java.nio.file.Path;
import java.util.List;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.InputChanges;

import com.matsuyoido.js.JsMinifyCompiler;
import com.matsuyoido.js.MinifyType;
import com.matsuyoido.plugin.frontend.extension.JavaScriptExtension;
import com.matsuyoido.plugin.frontend.extension.MinifierType;
import com.matsuyoido.plugin.frontend.extension.RootExtension;


@SuppressWarnings("javadoc")
public class JsMinifyTask extends DefaultTask {

	private final boolean					continueIfErrorExist;
	private final List<JavaScriptExtension>	settings;

	@Inject
	public JsMinifyTask() {
		final RootExtension extension = getProject().getExtensions().getByType(RootExtension.class);
		continueIfErrorExist = extension.getSkipError();
		settings = extension.getJSSetting();
	}

	@TaskAction
	public void compileJs(final InputChanges inputChanges) {
		settings.forEach(setting -> {
			setting.getOutputDirectory().mkdirs();
			final MinifyType minifyType = setting.getMinifierType() == MinifierType.YUI ? MinifyType.YUI : MinifyType.GOOGLE;
			final JsMinifyCompiler compiler = new JsMinifyCompiler(minifyType);
			new Minifier("js", false, continueIfErrorExist) {
				@Override
				protected String compile(final Path filePath) {
					return compiler.compile(filePath.toFile());
				}
			}.execute(setting.getInputDirectory(), setting.getOutputDirectory());
		});
	}

}
