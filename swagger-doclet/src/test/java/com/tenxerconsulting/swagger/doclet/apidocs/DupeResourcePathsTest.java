package com.tenxerconsulting.swagger.doclet.apidocs;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.javadoc.RootDoc;
import com.tenxerconsulting.swagger.doclet.DocletOptions;
import com.tenxerconsulting.swagger.doclet.JSONCompare;
import com.tenxerconsulting.swagger.doclet.Recorder;
import com.tenxerconsulting.swagger.doclet.parser.JaxRsAnnotationParser;

class DupeResourcePathsTest {

    private Recorder recorderMock;
    private DocletOptions options;

    @BeforeEach
    public void setup() {
        this.recorderMock = mock(Recorder.class);
        this.options = new DocletOptions().setRecorder(this.recorderMock).setIncludeSwaggerUi(false);
    }

    @Test
    void testStart() throws IOException {
        final RootDoc rootDoc = RootDocLoader.fromPath("src/test/resources", "fixtures.duperesourcepaths");

        boolean parsingResult = new JaxRsAnnotationParser(this.options, rootDoc).run();
        assertTrue(parsingResult);

        JSONCompare.compareListing("/fixtures/duperesourcepaths/service.json", recorderMock);

        JSONCompare.compare("/fixtures/duperesourcepaths/resource.json", recorderMock);
    }

}
