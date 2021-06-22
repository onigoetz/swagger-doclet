package com.tenxerconsulting.swagger.doclet.apidocs;

import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.javadoc.RootDoc;
import com.tenxerconsulting.swagger.doclet.DocletOptions;
import com.tenxerconsulting.swagger.doclet.JSONCompare;
import com.tenxerconsulting.swagger.doclet.Recorder;
import com.tenxerconsulting.swagger.doclet.parser.JaxRsAnnotationParser;

/**
 * The ResourceInclusionTest represents a test for resource class inclusion
 *
 * @author conor.roche
 * @version $Id$
 */
@SuppressWarnings("javadoc")
public class ResourceInclusionTest {

    private Recorder recorderMock;
    private DocletOptions options;

    @BeforeEach
    public void setup() {
        this.recorderMock = mock(Recorder.class);
        this.options = new DocletOptions().setRecorder(this.recorderMock).setIncludeSwaggerUi(false).setSortResourcesByPath(true)
                .setIncludeResourcePrefixes(Collections.singletonList("fixtures.resourceinclusion.pkg2"))
                .setExcludeResourcePrefixes(Collections.singletonList("fixtures.resourceinclusion.pkg2.Res3"));
    }

    @Test
    public void testStart() throws IOException {
        final RootDoc rootDoc = RootDocLoader.fromPath("src/test/resources", "fixtures.resourceinclusion");
        new JaxRsAnnotationParser(this.options, rootDoc).run();

        JSONCompare.compare("/fixtures/resourceinclusion/resourceinclusion.json", recorderMock);
    }

}
