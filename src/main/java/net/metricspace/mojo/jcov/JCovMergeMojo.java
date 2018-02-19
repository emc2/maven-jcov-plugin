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
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.sun.tdk.jcov.Merger;
import com.sun.tdk.jcov.data.Result;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "merge",
      defaultPhase = LifecyclePhase.PRE_SITE)
public class JCovMergeMojo extends AbstractJCovMojo {
    /**
     * Base name for combined coverage file.
     */
    @Parameter(property = "combinedCoverageFile",
               defaultValue = "${project.build.directory}/jcov/coverage/combined-coverage.xml",
               required = true)
    private File combinedCoverageFile;

    /**
     * Coverage results files.
     */
    @Parameter(property = "coverageFiles", required = true)
    private List<File> coverageFiles;

    private final Merger merger = new Merger();

    @Override
    public void execute() throws MojoExecutionException {
        final Result[] all = new Result[coverageFiles.size()];
        int i = 0;

        combinedCoverageFile.getParentFile().mkdirs();

        for(final File f : coverageFiles) {
            final Result result = new Result(f.getPath());
            all[i++] = result;
        }

        final Merger.Merge merge =
            new Merger.Merge(all, template.getPath());

        try {
            merger.mergeAndWrite(merge, null,
                                 combinedCoverageFile.getPath(), null);
        } catch(final IOException e) {
            throw new MojoExecutionException("IO error generating " +
                                             "combined coverage file", e);
        }
    }
}
