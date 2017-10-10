package io.github.apfelcreme.RegionReset.Exceptions;

import java.io.File;

public class MissingFileException extends Throwable {

    private final File file;

    public MissingFileException(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }
}
