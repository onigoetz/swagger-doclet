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

@SuppressWarnings("javadoc")
public class LongDatesTest {

    private Recorder recorderMock;
    private DocletOptions options;

    @BeforeEach
    public void setup() {
        this.recorderMock = mock(Recorder.class);
        this.options = new DocletOptions().setRecorder(this.recorderMock).setIncludeSwaggerUi(false);
        this.options.getLongTypePrefixes().add("java.util.Date");
        this.options.getLongTypePrefixes().add("java.util.Calendar");
    }

    @Test
    public void testStart() throws IOException {
        final RootDoc rootDoc = RootDocLoader.fromPath("src/test/resources", "fixtures.longdates");
        new JaxRsAnnotationParser(this.options, rootDoc).run();

        JSONCompare.compare("/fixtures/longdates/longdates.json", recorderMock);
    }

}
