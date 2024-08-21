
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
import com.matsuyoido.css.SassCompiler;
import com.matsuyoido.plugin.frontend.extension.RootExtension;
import com.matsuyoido.plugin.frontend.extension.ScssExtension;


@SuppressWarnings("javadoc")
public class SassCompileTask extends DefaultTask {
	private LineEnd				lineEnd;
	private boolean				continueIfErrorExist;
	private List<ScssExtension>	settings;
	private CanIUse				caniuse;

	void setupTask() throws IOException {
		final RootExtension extension = getProject().getExtensions().getByType(RootExtension.class);
		lineEnd = extension.getLineEndSetting();
		continueIfErrorExist = extension.getSkipError();
		settings = extension.getScssSetting();
		caniuse = new PrefixerCanIUse(getProject(), extension.getPrefixerSetting());
	}

	@TaskAction
	public void compileSass(final InputChanges inputChanges) throws IOException {
		setupTask();
		final SassCompiler compiler = new SassCompiler(lineEnd);
		final CssMinifyCompiler minifyCompiler = new CssMinifyCompiler(lineEnd, MinifyType.SIMPLE);
		final PrefixCompiler prefixerCompiler = new PrefixCompiler(caniuse.getCssSupports());

		settings.forEach(setting -> {
			setting.getOutputDirectory().mkdirs();
			final String exportExtension = setting.isEnableMinify() ? ".min.css" : "css";
			new Compiler(exportExtension, "glob:[!_]*.scss", null, continueIfErrorExist) {
				@Override
				protected String compile(final Path filePath) {
					String cssText = compiler.compile(filePath.toFile());
					if (setting.isAddPrefixer()) {
						cssText = prefixerCompiler.addPrefix(cssText);
					}
					if (setting.isEnableMinify()) {
						return minifyCompiler.compile(cssText);
					} else {
						return cssText;
					}
				}
			}.execute(setting.getInputDirectory(), setting.getOutputDirectory());
		});
	}

}
