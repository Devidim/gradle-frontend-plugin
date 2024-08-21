
package com.matsuyoido.plugin.frontend.task;

import java.util.function.Function;

import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Provider;
import org.gradle.work.FileChange;
import org.gradle.work.InputChanges;


/**
 * A mock implementation for testing tasks with InputChanges
 */
@SuppressWarnings("javadoc")
public class MockInputChanges implements InputChanges {

	private Function<FileCollection, Iterable<FileChange>>						   mFileChangeCollectionMapper;
	private Function<Provider<? extends FileSystemLocation>, Iterable<FileChange>> mFileChangeProviderMapper;
	private boolean																   mIncremental;

	public void setFileChangeCollectionMapper(final Function<FileCollection, Iterable<FileChange>> changeMapper) {
		mFileChangeCollectionMapper = changeMapper;
	}

	public void setFileChangeProviderMapper(final Function<Provider<? extends FileSystemLocation>, Iterable<FileChange>> changeMapper) {
		mFileChangeProviderMapper = changeMapper;
	}

	@Override
	public Iterable<FileChange> getFileChanges(final FileCollection parameter) {
		return mFileChangeCollectionMapper != null ? mFileChangeCollectionMapper.apply(parameter) : null;
	}

	@Override
	public Iterable<FileChange> getFileChanges(final Provider<? extends FileSystemLocation> parameter) {
		return mFileChangeProviderMapper != null ? mFileChangeProviderMapper.apply(parameter) : null;
	}

	public void setIncremental(final boolean incremental) {
		mIncremental = incremental;
	}

	@Override
	public boolean isIncremental() {
		return mIncremental;
	}

}
