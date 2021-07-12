package com.tenxerconsulting.swagger.doclet.apidocs;

import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.javadoc.RootDoc;
import com.tenxerconsulting.swagger.doclet.DocletOptions;
import com.tenxerconsulting.swagger.doclet.JSONCompare;
import com.tenxerconsulting.swagger.doclet.Recorder;
import com.tenxerconsulting.swagger.doclet.parser.JaxRsAnnotationParser;

/**
 * The ResourceExclusionTest represents a test for resource class exclusion
 *
 * @author conor.roche
 * @version $Id$
 */
class ResourceExclusionTest {

    private Recorder recorderMock;
    private DocletOptions options;

    @BeforeEach
    public void setup() {
        this.recorderMock = mock(Recorder.class);
        this.options = new DocletOptions().setRecorder(this.recorderMock).setIncludeSwaggerUi(false).setSortResourcesByPath(true)
                .setExcludeResourcePrefixes(Arrays.asList("fixtures.resourceexclusion.pkg1", "fixtures.resourceexclusion.pkg2.Res2a"));
    }

    @Test
    void testStart() throws IOException {
        final RootDoc rootDoc = RootDocLoader.fromPath("src/test/resources", "fixtures.resourceexclusion");
        new JaxRsAnnotationParser(this.options, rootDoc).run();

        JSONCompare.compareListing("/fixtures/resourceexclusion/resourceexclusion.json", recorderMock);
    }

}
