package com.matsuyoido.plugin.frontend.task;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

import com.matsuyoido.plugin.PathUtil;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * CompilerTest
 */
public class CompilerTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void getTargets_startsWith_Exclude() throws IOException {
        // case
        Compiler compiler = new Compiler("css", "glob:[!_]*.scss") {
            @Override
            protected String compile(Path filePath) {
                return null;
            }
        };
        String directory = PathUtil.classpathResourcePath("sassCompile");

        // execute
        Set<Path> result = compiler.getTargets(new File(directory));
        
        int expectFileCount = 2;
        assertThat(result).hasSize(expectFileCount);
    }

    @Test
    public void getTargets_minFileExclude() throws IOException {
        Compiler compiler = new Compiler("css", "glob:*[!.min].css") {
            @Override
            protected String compile(Path filePath) {
                return null;
            }
        };
        String directory = PathUtil.classpathResourcePath("minCompile");

        // execute
        Set<Path> result = compiler.getTargets(new File(directory));
        
        int expectFileCount = 2;
        assertThat(result).hasSize(expectFileCount);
    }

    @Test
    public void convertToOutputPath_nest() {
        // case
        String rootPath = "src/main/hogehoge";
        Path inputRootPath = Path.of(rootPath);
        Path outputRootPath = Path.of("src/test/hogehoge");
        Path inputFilePath = Path.of(rootPath, "test/test.tmp");

        Compiler compiler = new TestCompiler();

        // execute
        Path result = compiler.convetToOutputPath(inputRootPath, outputRootPath, inputFilePath);

        Path expect = Path.of("src/test/hogehoge", "test/test.tmp");
        assertThat(result.toString()).isEqualTo(expect.toString());
    }

    @Test
    public void outputFile() throws IOException {
        // case
        File outputDirectory = tempFolder.newFolder("compiledFolder");
        File outputFile = File.createTempFile("prefix", ".tmp", outputDirectory);
        String writeContent = "content";

        Compiler compiler = new TestCompiler();

        // execute
        compiler.outputFile(outputFile, writeContent);

        assertThat(outputFile).hasContent(writeContent);
    }


    private class TestCompiler extends Compiler {

        public TestCompiler() {
            super("tmp", "");
        }

        @Override
        protected String compile(Path filePath) {
            return null;
        }

    }
}