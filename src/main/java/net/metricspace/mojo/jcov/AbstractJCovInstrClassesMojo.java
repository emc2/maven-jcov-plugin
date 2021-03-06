/* Copyright (c) 2018, Eric McCorkle. All rights reserved.
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
package net.metricspace.mojo.jcov;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Common subclass for Mojo's that need to know where to find
 * instrumented classes.
 */
abstract class AbstractJCovInstrClassesMojo extends AbstractJCovMojo {
    /**
     * Maven variable for the default class directory.
     */
    private static final String DEFAULT_BASE_DIR =
        "project.build.outputDirectory";

    /**
     * Maven variable for the build directory.
     */
    private static final String DEFAULT_BUILD_DIR = "project.build.directory";

    /**
     * Subdirectory for default instrumented classes.
     */
    private static final String JCOV_CLASSES_DIR = "/jcov-classes";

    /**
     * Class instrumentations to perform.
     */
    @Parameter(property = "instrumentations")
    private List<Instrumentation> instrumentations;

    /**
     * The Maven project parameter.
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

    /**
     * Get the instrumentations to perform.
     */
    protected List<Instrumentation> getInstrumentations() {
        if (instrumentations.isEmpty()) {
            final File basedir =
                new File(project.getBuild().getOutputDirectory());
            final File instrdir =
                new File(new File(project.getBuild().getDirectory()),
                         JCOV_CLASSES_DIR);

            return Arrays.asList(new Instrumentation(basedir, instrdir));
        } else {
            return instrumentations;
        }
    }
}
