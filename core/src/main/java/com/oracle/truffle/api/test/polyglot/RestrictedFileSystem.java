/*
 * Copyright (c) 2017, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.oracle.truffle.api.test.polyglot;


import java.io.IOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import org.graalvm.polyglot.io.FileSystem;

public class RestrictedFileSystem extends ForwardingFileSystem {

    private final Path workingDirectory;
    private final Predicate<Path> readPredicate;
    private final Predicate<Path> writePredicate;

    public RestrictedFileSystem(
            final FileSystem delegate,
            final Path workingDirectory,
            final Predicate<Path> readPredicate,
            final Predicate<Path> writePredicate) {
        super(delegate);
        Objects.requireNonNull(readPredicate, "WorkingDirectory must be non null");
        Objects.requireNonNull(readPredicate, "ReadPredicate must be non null");
        Objects.requireNonNull(writePredicate, "WritePredicate must be non null");
        this.workingDirectory = workingDirectory;
        this.readPredicate = readPredicate;
        this.writePredicate = writePredicate;
    }

    @Override
    public Path parsePath(URI path) {
        return super.parsePath(path);
    }

    @Override
    public Path parsePath(String path) {
        Path parsedPath = super.parsePath(path);
        if(!parsedPath.isAbsolute()){
            parsedPath = workingDirectory.toAbsolutePath().resolve(parsedPath);
        }
        //if read isn't allow then don't parse the path and expose internal FS details
        if(!readPredicate.test(parsedPath)){
            return Paths.get(path);
        } else {
            return parsedPath;
        }
    }

    @Override
    public Path toRealPath(Path path, LinkOption... linkOptions) throws IOException {
        checkRead(path);
        return super.toRealPath(path, linkOptions);
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter) throws IOException {
        checkRead(dir);
        return super.newDirectoryStream(dir, filter);
    }

    @Override
    public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
        checkChannelOpenOptions(path, options);
        return super.newByteChannel(path, options, attrs);
    }

    @Override
    public void move(Path source, Path target, CopyOption... options) throws IOException {
        checkWrite(source);
        checkWrite(target);
        super.move(source, target, options);
    }

    @Override
    public void copy(Path source, Path target, CopyOption... options) throws IOException {
        checkRead(source);
        checkWrite(target);
        super.copy(source, target, options);
    }

    @Override
    public Path readSymbolicLink(Path link) throws IOException {
        checkReadLink(link);
        return super.readSymbolicLink(link);
    }

    @Override
    public void createSymbolicLink(Path link, Path target, FileAttribute<?>... attrs) throws IOException {
        checkWrite(link);
        super.createSymbolicLink(link, target, attrs);
    }

    @Override
    public void createLink(Path link, Path existing) throws IOException {
        checkWrite(link);
        checkWrite(existing);
        super.createLink(link, existing);
    }

    @Override
    public void delete(Path path) throws IOException {
        checkDelete(path);
        super.delete(path);
    }

    @Override
    public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
        checkWrite(dir);
        super.createDirectory(dir, attrs);
    }

    @Override
    public void checkAccess(Path path, Set<? extends AccessMode> modes, LinkOption... linkOptions) throws IOException {
        checkRead(path);
        super.checkAccess(path, modes, linkOptions);
    }

    @Override
    public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
        checkRead(path);
        return super.readAttributes(path, attributes, options);
    }

    @Override
    public void setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException {
        checkWrite(path);
        super.setAttribute(path, attribute, value, options);
    }

    private Path checkRead(Path path) {
        if (!readPredicate.test(path)) {
            throw new SecurityException("Read operation is not allowed for: " + path);
        }
        return path;
    }

    private Path checkWrite(Path path) {
        if (!writePredicate.test(path)) {
            throw new SecurityException("Write operation is not allowed for: " + path);
        }
        return path;
    }

    private Path checkDelete(Path path) {
        if (!writePredicate.test(path)) {
            throw new SecurityException("Delete operation is not allowed for: " + path);
        }
        return path;
    }

    private Path checkReadLink(Path path) {
        if (!readPredicate.test(path)) {
            throw new SecurityException("Read link operation is not allowed for: " + path);
        }
        return path;
    }

    private Path checkChannelOpenOptions(
            final Path path,
            final Set<? extends OpenOption> options) {
        boolean checkRead = options.contains(StandardOpenOption.READ);
        boolean checkWrite = options.contains(StandardOpenOption.WRITE);
        if (!checkRead && !checkWrite) {
            if (options.contains(StandardOpenOption.APPEND)) {
                checkWrite = true;
            } else {
                checkRead = true;
            }
        }
        if (checkRead) {
            checkRead(path);
        }
        if (checkWrite) {
            checkWrite(path);
        }
        if (options.contains(StandardOpenOption.DELETE_ON_CLOSE)) {
            checkDelete(path);
        }
        return path;
    }
}
