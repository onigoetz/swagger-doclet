package com.tenxerconsulting.swagger.doclet.apidocs;

import static com.tenxerconsulting.swagger.doclet.apidocs.FixtureLoader.loadFixture;
import static org.mockito.Mockito.mock;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.javadoc.RootDoc;
import com.tenxerconsulting.swagger.doclet.DocletOptions;
import com.tenxerconsulting.swagger.doclet.JSONCompare;
import com.tenxerconsulting.swagger.doclet.Recorder;
import com.tenxerconsulting.swagger.doclet.parser.JaxRsAnnotationParser;
import io.swagger.oas.models.info.Info;

class InfoTest {

    private Recorder recorderMock;
    private DocletOptions options;

    @BeforeEach
    public void setup() throws IOException {
        this.recorderMock = mock(Recorder.class);
        this.options = new DocletOptions().setRecorder(this.recorderMock).setIncludeSwaggerUi(false);

        final Info apiInfo = loadFixture("/fixtures/info/apiinfo.json", Info.class);
        this.options.setApiInfo(apiInfo);
    }

    @Test
    void testStart() throws IOException {
        final RootDoc rootDoc = RootDocLoader.fromPath("src/test/resources", "fixtures.info");
        new JaxRsAnnotationParser(this.options, rootDoc).run();

        JSONCompare.compareListing("/fixtures/info/service.json", recorderMock);
    }

}
