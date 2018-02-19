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
import java.util.Locale;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.sun.tdk.jcov.RepGen;
import com.sun.tdk.jcov.data.FileFormatException;
import com.sun.tdk.jcov.data.Result;
import com.sun.tdk.jcov.processing.ProcessingException;
import com.sun.tdk.jcov.report.ReportGenerator;
import com.sun.tdk.jcov.report.ReportGeneratorSPI;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;

@Mojo(name = "report",
      defaultPhase = LifecyclePhase.SITE)
public class JCovReport extends AbstractMavenReport {
    /**
     * Directory for generated reports.
     */
    @Parameter(property = "reportsDir",
               defaultValue = "${project.build.directory}/jcov/reports",
               required = true)
    private File reportsDir;

    /**
     * Format for generated reports.
     */
    @Parameter(property = "format",
               defaultValue= "")
    private String format;

    /**
     * Root directory of instrumented sources.
     */
    @Parameter(property = "srcRoot",
               defaultValue = "${project.build.sourceDirectory}",
               required = true)
    private File srcRoot;

    /**
     * Base name for combined coverage file.
     */
    @Parameter(property = "combinedCoverageFile",
               defaultValue = "${project.build.directory}/jcov/coverage/combined-coverage.xml",
               required = true)
    private File combinedCoverageFile;

    /**
     * Directory for generated coverage files.
     */
    @Parameter(property = "coverageFiles")
    private List<File> coverageFiles;

    /**
     * Regular expression for coverage files.
     */
    @Parameter(property = "coverageFileRegex",
               defaultValue= "([^\\.]+)-coverage\\.xml")
    private String coverageFileRegex;

    private final RepGen repgen = new RepGen();

    @Override
    public String getOutputName() {
        return "jcov-report";
    }

    @Override
    public String getName(final Locale locale) {
        return "JCov Coverage Reports";
    }

    @Override
    public String getDescription(Locale locale) {
        return "Coverage reports for test suites";
    }

    private void makeReport(final String actualFormat,
                            final Pattern pattern,
                            final File f)
        throws MavenReportException {
        final Matcher matcher = pattern.matcher(f.getName());

        if (!f.isFile()) {
            getLog().warn("Report file " + f.getPath() +
                          " does not exist");
            return;
        }

        if (!matcher.matches()) {
            getLog().warn("Report file " + f.getPath() +
                          " does not match report file regex");
            return;
        }

        if(matcher.groupCount() != 1) {
            throw new MavenReportException("Coverage file regex did not " +
                                           "match exactly one group");
        }

        final String repname = matcher.group(1);
        final File reportDir = new File(reportsDir, repname);
        final Result result = new Result(f.getPath());

        try {
            getLog().info("Generating report " + repname);
            repgen.generateReport(actualFormat, reportDir.getPath(),
                                  result, srcRoot.getPath());
        } catch(final ProcessingException e) {
            throw new MavenReportException("Error processing coverage file " +
                                           f.getPath(), e);
        } catch(final FileFormatException e) {
            throw new MavenReportException("Invalid coverage file " +
                                           f.getPath(), e);
        } catch(final Exception e) {
            throw new MavenReportException("Exception generating report for " +
                                           f.getPath(), e);
        }
    }

    @Override
    public void executeReport(final Locale locale)
        throws MavenReportException {
        final String actualFormat = format == null ? null :
            (format.equals("") ? null : format);
        final Pattern pattern = Pattern.compile(coverageFileRegex);

        reportsDir.mkdirs();

        for(final File f : coverageFiles) {
            makeReport(actualFormat, pattern, f);
        }

        makeReport(actualFormat, pattern, combinedCoverageFile);
    }
}
