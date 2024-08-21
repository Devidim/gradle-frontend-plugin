
package com.matsuyoido.plugin.frontend.task;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import com.matsuyoido.caniuse.SupportData;
import com.matsuyoido.plugin.PathUtil;
import com.matsuyoido.plugin.frontend.extension.PrefixerExtension;


/**
 * PrefixerCanIUseTest
 */
public class PrefixerCanIUseTest {

	@Test
	public void getCssSupport() throws IOException {
		final PrefixerExtension extension = new PrefixerExtension();
		extension.setCaniuseData(new File(PathUtil.classpathResourcePath("caniuse/data.json")));
		extension.setChrome("all");

		final PrefixerCanIUse caniuse = new PrefixerCanIUse(ProjectBuilder.builder().build(), extension);

		final List<SupportData> result = caniuse.getCssSupports();

		assertThat(result).isNotEmpty();
	}

	@Test
	public void getCssSupport_notSettingCaniue() throws IOException {
		final PrefixerExtension extension = new PrefixerExtension();
		extension.setChrome("all");

		final PrefixerCanIUse caniuse = new PrefixerCanIUse(ProjectBuilder.builder().build(), extension);

		final List<SupportData> result = caniuse.getCssSupports();

		assertThat(result).isNotEmpty();
	}

}
