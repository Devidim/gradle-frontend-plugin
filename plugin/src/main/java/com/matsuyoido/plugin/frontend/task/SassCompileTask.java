package com.matsuyoido.plugin.frontend.task;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import com.matsuyoido.LineEnd;
import com.matsuyoido.caniuse.CanIUse;
import com.matsuyoido.css.CssMinifyCompiler;
import com.matsuyoido.css.MinifyType;
import com.matsuyoido.css.PrefixCompiler;
import com.matsuyoido.css.SassCompiler;
import com.matsuyoido.plugin.frontend.extension.ScssExtension;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.incremental.IncrementalTaskInputs;

public class SassCompileTask extends DefaultTask {
    private LineEnd lineEnd;
    private List<ScssExtension> settings;
    private Optional<CanIUse> caniuse;

    @Inject
    public SassCompileTask(LineEnd lineEnd, List<ScssExtension> settings, CanIUse caniuse) {
        this.lineEnd = lineEnd;
        this.settings = settings;
        this.caniuse = Optional.ofNullable(caniuse);
    }

    @TaskAction
    public void compileSass(IncrementalTaskInputs inputs) {
        SassCompiler compiler = new SassCompiler(lineEnd);
        CssMinifyCompiler minifyCompiler = new CssMinifyCompiler(lineEnd, MinifyType.SIMPLE);
        PrefixCompiler prefixerCompiler = caniuse.map(v -> new PrefixCompiler(v.getCssSupports())).orElse(null);

        this.settings.forEach(setting -> {
            setting.getOutputDirectory()
                   .mkdirs();
            String exportExtension = setting.isEnableMinify() ? ".min.css" : "css";
            new Compiler(exportExtension, "glob:[!_]*.scss"){
                @Override
                protected String compile(Path filePath) {
                    String cssText = compiler.compile(filePath.toFile());
                    if (setting.isAddPrefixer()) {
                        if (!caniuse.isPresent()) {
                            throw new IllegalStateException("Not Found caniuse data.");
                        }
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