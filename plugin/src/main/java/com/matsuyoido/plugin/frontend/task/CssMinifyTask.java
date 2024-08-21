
package com.matsuyoido.plugin.frontend.task;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.InputChanges;

import com.matsuyoido.LineEnd;
import com.matsuyoido.caniuse.CanIUse;
import com.matsuyoido.css.CssMinifyCompiler;
import com.matsuyoido.css.MinifyType;
import com.matsuyoido.css.PrefixCompiler;
import com.matsuyoido.plugin.frontend.extension.CssExtension;
import com.matsuyoido.plugin.frontend.extension.RootExtension;


@SuppressWarnings("javadoc")
public class CssMinifyTask extends DefaultTask {

	private LineEnd			   lineEnd;
	private boolean			   continueIfErrorExist;
	private List<CssExtension> settings;
	private CanIUse			   caniuse;

	void setupTask() throws IOException {
		final RootExtension extension = getProject().getExtensions().getByType(RootExtension.class);
		lineEnd = extension.getLineEndSetting();
		continueIfErrorExist = extension.getSkipError();
		settings = extension.getCssSetting();
		caniuse = new PrefixerCanIUse(getProject(), extension.getPrefixerSetting());
	}

	@TaskAction
	public void minifyCss(final InputChanges inputChanges) throws IOException {
		setupTask();
		final PrefixCompiler prefixer = new PrefixCompiler(caniuse.getCssSupports());
		final CssMinifyCompiler compiler = new CssMinifyCompiler(lineEnd, MinifyType.YUI);

		settings.forEach(setting -> {
			setting.getOutputDirectory().mkdirs();
			new Minifier("css", false, continueIfErrorExist) {
				@Override
				protected String compile(final Path filePath) {
					if (setting.isAddPrefixer()) {
						return compiler.compile(prefixer.addPrefix(filePath.toFile()));
					} else {
						return compiler.compile(filePath.toFile());
					}
				}
			}.execute(setting.getInputDirectory(), setting.getOutputDirectory());
		});
	}

}
