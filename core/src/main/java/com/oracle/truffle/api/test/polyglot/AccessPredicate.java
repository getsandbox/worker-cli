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

import java.nio.file.Path;
import java.util.Collection;
import java.util.function.Predicate;

public class AccessPredicate implements Predicate<Path> {
    private final Collection<? extends Path> allowedRoots;

    public AccessPredicate(
            final Collection<? extends Path> allowedRoots) {
        this.allowedRoots = allowedRoots;
    }

    @Override
    @SuppressWarnings("fallthrough")
    public boolean test(Path path) {
        return getOwnerRoot(path, allowedRoots) != null;
    }

    private static Path getOwnerRoot(final Path path, final Collection<? extends Path> roots) {
        final Path absolutePath = path.toAbsolutePath().normalize();
        for (Path root : roots) {
            Path absoluteRoot = root.toAbsolutePath().normalize();
            for (Path currentPath = absolutePath; currentPath != null; currentPath = currentPath.getParent()) {
                if (currentPath.equals(absoluteRoot)) {
                    return root;
                }
            }
        }
        return null;
    }
}
