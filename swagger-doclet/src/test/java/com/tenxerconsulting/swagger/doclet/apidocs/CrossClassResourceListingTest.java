package com.tenxerconsulting.swagger.doclet.apidocs;

import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.javadoc.RootDoc;
import com.tenxerconsulting.swagger.doclet.DocletOptions;
import com.tenxerconsulting.swagger.doclet.JSONCompare;
import com.tenxerconsulting.swagger.doclet.Recorder;
import com.tenxerconsulting.swagger.doclet.parser.JaxRsAnnotationParser;

/**
 * The CrossClassResourceListingTest represents a test of ordering and descriptions for cross class
 * resources
 *
 * @author conor.roche
 * @version $Id$
 */
class CrossClassResourceListingTest {

    private Recorder recorderMock;
    private DocletOptions options;

    @BeforeEach
    public void setup() {
        this.recorderMock = mock(Recorder.class);
        this.options = new DocletOptions().setRecorder(this.recorderMock).setIncludeSwaggerUi(false);
    }

    @Test
    void testDefaultOrder() throws IOException {
        this.options.setSortResourcesByPath(false);
        this.options.setSortResourcesByPriority(false);

        final RootDoc rootDoc = RootDocLoader.fromPath("src/test/resources", "fixtures.crossclassresourcelisting");
        new JaxRsAnnotationParser(this.options, rootDoc).run();

        JSONCompare.compareListing("/fixtures/crossclassresourcelisting/service.json", recorderMock);
    }

    @Test
    void testPriorityOrder() throws IOException {
        this.options.setSortResourcesByPath(false);
        this.options.setSortResourcesByPriority(true);

        final RootDoc rootDoc = RootDocLoader.fromPath("src/test/resources", "fixtures.crossclassresourcelisting");
        new JaxRsAnnotationParser(this.options, rootDoc).run();

        JSONCompare.compareListing("/fixtures/crossclassresourcelisting/service2.json", recorderMock);
    }

    @Test
    void testPathOrder() throws IOException {
        this.options.setSortResourcesByPath(true);
        this.options.setSortResourcesByPriority(false);

        final RootDoc rootDoc = RootDocLoader.fromPath("src/test/resources", "fixtures.crossclassresourcelisting");
        new JaxRsAnnotationParser(this.options, rootDoc).run();

        JSONCompare.compareListing("/fixtures/crossclassresourcelisting/service3.json", recorderMock);
    }
}
