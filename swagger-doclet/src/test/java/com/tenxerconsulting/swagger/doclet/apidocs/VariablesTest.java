package com.tenxerconsulting.swagger.doclet.apidocs;

import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.Properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.javadoc.RootDoc;
import com.tenxerconsulting.swagger.doclet.DocletOptions;
import com.tenxerconsulting.swagger.doclet.JSONCompare;
import com.tenxerconsulting.swagger.doclet.Recorder;
import com.tenxerconsulting.swagger.doclet.parser.JaxRsAnnotationParser;

/**
 * The VariablesTest represents a test case for replacing variables in the javadoc with values sourced from an external properties
 * file.
 *
 * @author conor.roche
 * @version $Id$
 */
@SuppressWarnings("javadoc")
public class VariablesTest {

    private Recorder recorderMock;
    private DocletOptions options;

    @BeforeEach
    public void setup() {
        this.recorderMock = mock(Recorder.class);
        Properties variableReplacements = new Properties();
        variableReplacements.setProperty("v1", "v1val");
        variableReplacements.setProperty("v2", "v2val");
        variableReplacements.setProperty("v3", "VALUE1");
        variableReplacements.setProperty("v4", "v4val");
        this.options = new DocletOptions().setRecorder(this.recorderMock).setIncludeSwaggerUi(false).setVariableReplacements(variableReplacements);
    }

    @Test
    public void testStart() throws IOException {
        final RootDoc rootDoc = RootDocLoader.fromPath("src/test/resources", "fixtures.variables");
        new JaxRsAnnotationParser(this.options, rootDoc).run();

        JSONCompare.compare("/fixtures/variables/variables.json", recorderMock);
    }

}
