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
import com.tenxerconsulting.swagger.doclet.parser.NamingConvention;

class NamingConventionTest {

    private Recorder recorderMock;
    private DocletOptions options;

    @BeforeEach
    public void setup() {
        this.recorderMock = mock(Recorder.class);
    }

    @Test
    void testLowerUnderscore() throws IOException {

        this.options = new DocletOptions().setRecorder(this.recorderMock).setIncludeSwaggerUi(false)
                .setModelFieldsNamingConvention(NamingConvention.LOWER_UNDERSCORE);

        final RootDoc rootDoc = RootDocLoader.fromPath("src/test/resources", "fixtures.namingconvention.lowerunderscore");
        new JaxRsAnnotationParser(this.options, rootDoc).run();

        JSONCompare.compare("/fixtures/namingconvention/lowerunderscore/lowerunderscore.json", recorderMock);
    }

    @Test
    void testLowerUnderscoreUnless() throws IOException {

        this.options = new DocletOptions().setRecorder(this.recorderMock).setIncludeSwaggerUi(false)
                .setModelFieldsNamingConvention(NamingConvention.LOWER_UNDERSCORE_UNLESS_OVERRIDDEN);

        final RootDoc rootDoc = RootDocLoader.fromPath("src/test/resources", "fixtures.namingconvention.lowerunderscoreunless");
        new JaxRsAnnotationParser(this.options, rootDoc).run();

        JSONCompare.compare("/fixtures/namingconvention/lowerunderscoreunless/lowerunderscoreunless.json", recorderMock);
    }

    @Test
    void testLower() throws IOException {

        this.options = new DocletOptions().setRecorder(this.recorderMock).setIncludeSwaggerUi(false).setModelFieldsNamingConvention(NamingConvention.LOWERCASE);

        final RootDoc rootDoc = RootDocLoader.fromPath("src/test/resources", "fixtures.namingconvention.lower");
        new JaxRsAnnotationParser(this.options, rootDoc).run();

        JSONCompare.compare("/fixtures/namingconvention/lower/lower.json", recorderMock);
    }

    @Test
    void testLowerUnless() throws IOException {

        this.options = new DocletOptions().setRecorder(this.recorderMock).setIncludeSwaggerUi(false)
                .setModelFieldsNamingConvention(NamingConvention.LOWERCASE_UNLESS_OVERRIDDEN);

        final RootDoc rootDoc = RootDocLoader.fromPath("src/test/resources", "fixtures.namingconvention.lowerunless");
        new JaxRsAnnotationParser(this.options, rootDoc).run();

        JSONCompare.compare("/fixtures/namingconvention/lowerunless/lowerunless.json", recorderMock);
    }

    @Test
    void testUpper() throws IOException {

        this.options = new DocletOptions().setRecorder(this.recorderMock).setIncludeSwaggerUi(false).setModelFieldsNamingConvention(NamingConvention.UPPERCASE);

        final RootDoc rootDoc = RootDocLoader.fromPath("src/test/resources", "fixtures.namingconvention.upper");
        new JaxRsAnnotationParser(this.options, rootDoc).run();

        JSONCompare.compare("/fixtures/namingconvention/upper/upper.json", recorderMock);
    }

    @Test
    void testUpperUnless() throws IOException {

        this.options = new DocletOptions().setRecorder(this.recorderMock).setIncludeSwaggerUi(false)
                .setModelFieldsNamingConvention(NamingConvention.UPPERCASE_UNLESS_OVERRIDDEN);

        final RootDoc rootDoc = RootDocLoader.fromPath("src/test/resources", "fixtures.namingconvention.upperunless");
        new JaxRsAnnotationParser(this.options, rootDoc).run();

        JSONCompare.compare("/fixtures/namingconvention/upperunless/upperunless.json", recorderMock);
    }

}
