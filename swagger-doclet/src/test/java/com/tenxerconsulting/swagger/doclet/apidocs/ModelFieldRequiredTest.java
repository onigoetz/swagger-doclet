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
 * The ModelFieldRequiredTest represents a test for required vs optional model fields
 *
 * @author conor.roche
 * @version $Id$
 */
@SuppressWarnings("javadoc")
public class ModelFieldRequiredTest {

    private Recorder recorderMock;
    private DocletOptions options;

    @BeforeEach
    public void setup() {
        this.recorderMock = mock(Recorder.class);
        this.options = new DocletOptions().setRecorder(this.recorderMock).setIncludeSwaggerUi(false);
    }

    @Test
    public void testModelFieldNotRequiredByDefault() throws IOException {
        this.options.setModelFieldsRequiredByDefault(false);

        final RootDoc rootDoc = RootDocLoader.fromPath("src/test/resources", "fixtures.modelfieldrequired");
        new JaxRsAnnotationParser(this.options, rootDoc).run();

        JSONCompare.compare("/fixtures/modelfieldrequired/modelfieldrequired.json", recorderMock);
    }

    @Test
    public void testModelFieldRequiredByDefault() throws IOException {
        this.options.setModelFieldsRequiredByDefault(true);

        final RootDoc rootDoc = RootDocLoader.fromPath("src/test/resources", "fixtures.modelfieldrequired");
        new JaxRsAnnotationParser(this.options, rootDoc).run();

        JSONCompare.compare("/fixtures/modelfieldrequired/modelfieldrequired2.json", recorderMock);
    }

}
