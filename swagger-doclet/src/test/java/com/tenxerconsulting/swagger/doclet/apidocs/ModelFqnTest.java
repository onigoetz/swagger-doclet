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
public class ModelFqnTest {

    private Recorder recorderMock;
    private DocletOptions options;

    @BeforeEach
    public void setup() {
        this.recorderMock = mock(Recorder.class);
        this.options = new DocletOptions().setRecorder(this.recorderMock).setIncludeSwaggerUi(false);
    }

    @Test
    public void testShortModelId() throws IOException {

        this.options.setUseFullModelIds(false);

        final RootDoc rootDoc = RootDocLoader.fromPath("src/test/resources", "fixtures.modelfqn");
        new JaxRsAnnotationParser(this.options, rootDoc).run();

        JSONCompare.compare("/fixtures/modelfqn/modelsn.json", recorderMock);
    }

    @Test
    public void testFullModelId() throws IOException {

        this.options.setUseFullModelIds(true);

        final RootDoc rootDoc = RootDocLoader.fromPath("src/test/resources", "fixtures.modelfqn");
        new JaxRsAnnotationParser(this.options, rootDoc).run();

        JSONCompare.compare("/fixtures/modelfqn/modelfqn.json", recorderMock);
    }

}
