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
import java.io.InputStream;
import java.io.IOException;
import java.nio.file.Files;

import com.sun.tdk.jcov.Instr;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Instrument phase.  This instruments a set of classes, adding the
 * coverage-counting bytecodes.  This phase is designed to generate a
 * mirror class directory containing the instrumented classes, and
 * should run after the compile phase, but before test-compile.
 */
@Mojo(name = "instrument",
      defaultPhase = LifecyclePhase.PROCESS_CLASSES)
public class JCovInstrumentMojo extends AbstractJCovInstrClassesMojo {
    /**
     * Resource path to file saver JAR.
     */
    private static final String FILE_SAVER_JAR_NAME = "/jcov_file_saver.jar";

    /**
     * Path at which the file saver JAR will be created.
     */
    @Parameter(property = "jcovFileSaverJar",
               defaultValue =
               "${project.build.directory}/jcov/jcov_file_saver.jar",
               required = true)
    private File jcovFileSaverJar;

    /**
     * Instrumenter to use.
     */
    private final Instr instr = new Instr();

    /**
     * Create the file saver JAR.
     */
    private void createFileSaverJar() throws MojoExecutionException {
        try {
            if (!jcovFileSaverJar.isFile()) {
                getLog().info("Creating file saver jar at " +
                              jcovFileSaverJar.getPath());
                jcovFileSaverJar.getParentFile().mkdirs();

                try(final InputStream ins =
                    getClass().getResourceAsStream(FILE_SAVER_JAR_NAME)) {
                    Files.copy(ins, jcovFileSaverJar.toPath());
                }
            } else {
                getLog().info("File saver jar " + jcovFileSaverJar.getPath() +
                              " already exists");
            }
        } catch(final IOException e) {
            throw new MojoExecutionException("Error saving file saver jar", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void execute() throws MojoExecutionException {
        createFileSaverJar();

        try {
            for(final Instrumentation in : getInstrumentations()) {
                final File baseDir = in.getBaseDirectory();
                final File instrDir = in.getInstrDirectory();

                getLog().info("Instrumenting classes in " + baseDir.getPath() +
                              ", outputting to " + instrDir.getPath());
                getTemplate().getParentFile().mkdirs();
                instr.instrumentFiles(new File[] { baseDir }, instrDir, null);
                instr.finishWork(getTemplate().getPath());
            }
        } catch(final IOException e) {
            throw new MojoExecutionException(
                "IO error while instrumenting classes", e);
        }
    }
}
