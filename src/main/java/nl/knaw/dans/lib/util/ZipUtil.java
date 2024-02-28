/*
 * Copyright (C) 2021 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.lib.util;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.IOUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class ZipUtil {

    public static void zipDirectory(Path sourceDirectory, String targetZipFile, boolean oneRootDir) throws IOException {
        Path zipFilePath = Paths.get(targetZipFile);
        try (OutputStream outputStream = Files.newOutputStream(zipFilePath)) {
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
            try (ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(bufferedOutputStream)) {
                String base;
                if (oneRootDir) {
                    zipArchiveOutputStream.putArchiveEntry(new ZipArchiveEntry(sourceDirectory.toFile(), sourceDirectory.getFileName().toString() + "/"));
                    zipArchiveOutputStream.closeArchiveEntry();
                    base = sourceDirectory.getFileName().toString() + "/";
                }
                else {
                    base = "";
                }
                try (Stream<Path> paths = Files.list(sourceDirectory)) {
                    paths.forEach(path -> {
                        try {
                            addFileToZipStream(zipArchiveOutputStream, path.toFile(), base);
                        }
                        catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            }
        }
    }

    private static void addFileToZipStream(ZipArchiveOutputStream zipArchiveOutputStream, File fileToZip, String base) throws IOException {
        String entryName = base + fileToZip.getName();
        ZipArchiveEntry zipArchiveEntry = new ZipArchiveEntry(fileToZip, entryName);
        zipArchiveOutputStream.putArchiveEntry(zipArchiveEntry);
        if (fileToZip.isFile()) {
            try (InputStream fileInputStream = Files.newInputStream(fileToZip.toPath())) {
                IOUtils.copy(fileInputStream, zipArchiveOutputStream);
                zipArchiveOutputStream.closeArchiveEntry();
            }
        }
        else {
            zipArchiveOutputStream.closeArchiveEntry();
            File[] files = fileToZip.listFiles();
            if (files != null) {
                for (File file : files) {
                    addFileToZipStream(zipArchiveOutputStream, file, entryName + "/");
                }
            }
        }
    }
}
