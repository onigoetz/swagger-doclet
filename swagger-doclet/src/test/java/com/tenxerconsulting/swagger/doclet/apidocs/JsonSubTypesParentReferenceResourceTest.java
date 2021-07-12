package com.tenxerconsulting.swagger.doclet.apidocs;

import static org.junit.jupiter.api.Assertions.assertNotNull;
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

class JsonSubTypesParentReferenceResourceTest {
    private Recorder recorderMock;
    private DocletOptions options;

    @BeforeEach
    public void setup() {
        this.recorderMock = mock(Recorder.class);
        //this.recorderMock = new ObjectMapperRecorder(null, null, null, null);
        String[][] additionalParams = new String[][]{
                //{"-d", "."}
        };
        this.options = DocletOptions.parse(additionalParams);
        this.options = options.setRecorder(this.recorderMock).setIncludeSwaggerUi(false);
    }

    @Test
    void testStart() throws IOException {
        final RootDoc rootDoc = RootDocLoader.fromPath("src/test/resources", "fixtures.jsonsubtypesparentreference");
        assertNotNull(rootDoc);
        boolean result = new JaxRsAnnotationParser(this.options, rootDoc).run();
        assertTrue(result);

        JSONCompare.compare("/fixtures/jsonsubtypesparentreference/node.json", recorderMock);
    }
}
