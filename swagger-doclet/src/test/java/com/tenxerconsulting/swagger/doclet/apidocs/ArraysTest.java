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

class ArraysTest {

    private Recorder recorderMock;
    private DocletOptions options;

    // only have this above 1 for profiling
    private int numExecutions = 1;

    @BeforeEach
    public void setup() {
        this.recorderMock = mock(Recorder.class);
        this.options = new DocletOptions().setRecorder(this.recorderMock).setIncludeSwaggerUi(false);
    }

    @Test
    void testStart() throws IOException {
        final RootDoc rootDoc = RootDocLoader.fromPath("src/test/resources", "fixtures.arrays");

        JaxRsAnnotationParser parser = new JaxRsAnnotationParser(this.options, rootDoc);
        for (int i = 0; i < this.numExecutions; i++) {
            parser.run();
        }
        JSONCompare.compare("/fixtures/arrays/arrays.json", recorderMock);
    }

}
