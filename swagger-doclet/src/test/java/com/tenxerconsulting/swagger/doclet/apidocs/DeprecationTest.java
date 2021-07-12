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

class DeprecationTest {

    private Recorder recorderMock;
    private DocletOptions options;

    @BeforeEach
    public void setup() {
        this.recorderMock = mock(Recorder.class);
        this.options = new DocletOptions().setRecorder(this.recorderMock).setIncludeSwaggerUi(false);
    }

    @Test
    void testDeprecationEnabled() throws IOException {
        this.options.setExcludeDeprecatedOperations(true);
        this.options.setExcludeDeprecatedParams(true);
        this.options.setExcludeDeprecatedFields(true);

        final RootDoc rootDoc = RootDocLoader.fromPath("src/test/resources", "fixtures.deprecation");
        new JaxRsAnnotationParser(this.options, rootDoc).run();

        JSONCompare.compare("/fixtures/deprecation/deprecation.json", recorderMock);
    }

    @Test
    void testDeprecationDisabled() throws IOException {

        this.options.setExcludeDeprecatedOperations(false);
        this.options.setExcludeDeprecatedParams(false);
        this.options.setExcludeDeprecatedFields(false);

        final RootDoc rootDoc = RootDocLoader.fromPath("src/test/resources", "fixtures.deprecation");
        new JaxRsAnnotationParser(this.options, rootDoc).run();

        JSONCompare.compare("/fixtures/deprecation/deprecation2.json", recorderMock);
    }

}
