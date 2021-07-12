package com.tenxerconsulting.swagger.doclet.apidocs;

import static org.mockito.Mockito.mock;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.javadoc.RootDoc;
import com.tenxerconsulting.swagger.doclet.DocletOptions;
import com.tenxerconsulting.swagger.doclet.JSONCompare;
import com.tenxerconsulting.swagger.doclet.Recorder;
import com.tenxerconsulting.swagger.doclet.parser.JaxRsAnnotationParser;

/**
 * The ResourceListingTest represents
 *
 * @author conor.roche
 * @version $Id$
 */
class ResourceListingTest {

    private Recorder recorderMock;
    private DocletOptions options;

    @BeforeEach
    public void setup() {
        System.out.println("ResourceListingTest setup");
        this.recorderMock = mock(Recorder.class);
        this.options = new DocletOptions().setRecorder(this.recorderMock).setIncludeSwaggerUi(false);
    }

    @Test
    void testPriorityOrder() throws IOException {
        this.options.getResourceDescriptionTags().add("resourceDescription");
        this.options.getResourcePriorityTags().add("resourcePriority");
        this.options.setSortResourcesByPath(false);
        this.options.setSortResourcesByPriority(true);

        final RootDoc rootDoc = RootDocLoader.fromPath("src/test/resources", "fixtures.resourcelisting");
        new JaxRsAnnotationParser(this.options, rootDoc).run();

        JSONCompare.compareListing("/fixtures/resourcelisting/service2.json", recorderMock);
    }

    @Test
    void testPathOrder() throws IOException {
        this.options.getResourceDescriptionTags().add("resourceDescription");
        this.options.getResourcePriorityTags().add("resourcePriority");
        this.options.setSortResourcesByPath(true);
        this.options.setSortResourcesByPriority(false);

        final RootDoc rootDoc = RootDocLoader.fromPath("src/test/resources", "fixtures.resourcelisting");
        new JaxRsAnnotationParser(this.options, rootDoc).run();

        JSONCompare.compareListing("/fixtures/resourcelisting/service3.json", recorderMock);
    }

}
