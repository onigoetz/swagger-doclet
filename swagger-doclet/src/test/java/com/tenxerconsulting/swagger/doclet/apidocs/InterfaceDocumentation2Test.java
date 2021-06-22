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
 * Like the InterfaceDocumentation, but the JaxRS annotations are on the base class instead of on the interface.
 */
@SuppressWarnings("javadoc")
public class InterfaceDocumentation2Test {

    private Recorder recorderMock;
    private DocletOptions options;

    @BeforeEach
    public void setup() {
        this.recorderMock = mock(Recorder.class);
        this.options = new DocletOptions().setRecorder(this.recorderMock).setIncludeSwaggerUi(false);
    }

    @Test
    public void testInterfaceDocumentation() throws IOException {
        final RootDoc rootDoc = RootDocLoader.fromPath("src/test/resources", "fixtures.interfacedocumentation2");
        new JaxRsAnnotationParser(this.options, rootDoc).run();

        JSONCompare.compare("/fixtures/interfacedocumentation2/interfacedocumentation2.json", recorderMock);
    }
}
