package com.tenxerconsulting.swagger.doclet.apidocs;

import static com.tenxerconsulting.swagger.doclet.apidocs.FixtureLoader.loadFixture;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.sun.javadoc.RootDoc;
import com.tenxerconsulting.swagger.doclet.DocletOptions;
import com.tenxerconsulting.swagger.doclet.JSONCompare;
import com.tenxerconsulting.swagger.doclet.Recorder;
import com.tenxerconsulting.swagger.doclet.parser.JaxRsAnnotationParser;
import io.swagger.oas.models.security.SecurityScheme;

@SuppressWarnings("javadoc")
public class OAuth2Test {

    private Recorder recorderMock;
    private DocletOptions options;

    @BeforeEach
    public void setup() throws IOException {
        this.recorderMock = mock(Recorder.class);
        this.options = new DocletOptions().setRecorder(this.recorderMock).setIncludeSwaggerUi(false);

        MapType type = TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, SecurityScheme.class);

        final Map<String, SecurityScheme> securitySchemes = loadFixture("/fixtures/oauth2/apiauth.json", type);
        this.options.setSecuritySchemes(securitySchemes);
        this.options.getAuthOperationScopes().add("read:pets");
    }

    @Test
    public void testStart() throws IOException {
        final RootDoc rootDoc = RootDocLoader.fromPath("src/test/resources", "fixtures.oauth2");
        new JaxRsAnnotationParser(this.options, rootDoc).run();

        JSONCompare.compareListing("/fixtures/oauth2/service.json", recorderMock);

        JSONCompare.compare("/fixtures/oauth2/oauth2.json", recorderMock);
    }

}
