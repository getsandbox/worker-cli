package com.sandbox.worker.test;

import java.io.File;
import java.nio.file.Paths;
import java.security.CodeSource;

public class TestFileUtils {

    public static File getFile(Class target, String filePath) {
        CodeSource codeSource = target.getProtectionDomain().getCodeSource();
        File classFile = new File(codeSource.getLocation().getFile());
        File file = null;
        while (file == null) {

            File possibleFile = Paths.get(classFile.getAbsolutePath(), filePath).toFile();
            if (possibleFile.exists()) {
                file = possibleFile;
                System.out.println("Found file: " + file.getAbsolutePath());
            } else {
                classFile = classFile.getParentFile();
            }
        }

        return file;
    }

}
