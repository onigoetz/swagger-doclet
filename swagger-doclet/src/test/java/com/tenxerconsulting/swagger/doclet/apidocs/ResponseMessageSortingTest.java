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
import com.tenxerconsulting.swagger.doclet.parser.ResponseMessageSortMode;

@SuppressWarnings("javadoc")
public class ResponseMessageSortingTest {

    private Recorder recorderMock;
    private DocletOptions options;

    @BeforeEach
    public void setup() {
        this.recorderMock = mock(Recorder.class);
        this.options = new DocletOptions().setRecorder(this.recorderMock).setIncludeSwaggerUi(false);
    }

    @Test
    public void testDefaultSorting() throws IOException {
        final RootDoc rootDoc = RootDocLoader.fromPath("src/test/resources", "fixtures.responsemessagesorting");
        new JaxRsAnnotationParser(this.options, rootDoc).run();

        JSONCompare.compare("/fixtures/responsemessagesorting/responsemessagesortingasc.json", recorderMock);
    }

    @Test
    public void testAscSortingViaOption() throws IOException {

        this.options.setResponseMessageSortMode(ResponseMessageSortMode.CODE_ASC);

        final RootDoc rootDoc = RootDocLoader.fromPath("src/test/resources", "fixtures.responsemessagesorting");
        new JaxRsAnnotationParser(this.options, rootDoc).run();

        JSONCompare.compare("/fixtures/responsemessagesorting/responsemessagesortingasc.json", recorderMock);
    }

    @Test
    public void testAsAppearsSorting() throws IOException {

        this.options.setResponseMessageSortMode(ResponseMessageSortMode.AS_APPEARS);

        final RootDoc rootDoc = RootDocLoader.fromPath("src/test/resources", "fixtures.responsemessagesorting");
        new JaxRsAnnotationParser(this.options, rootDoc).run();

        JSONCompare.compare("/fixtures/responsemessagesorting/responsemessagesortingasappears.json", recorderMock);
    }

    @Test
    public void testDescSorting() throws IOException {

        this.options.setResponseMessageSortMode(ResponseMessageSortMode.CODE_DESC);

        final RootDoc rootDoc = RootDocLoader.fromPath("src/test/resources", "fixtures.responsemessagesorting");
        new JaxRsAnnotationParser(this.options, rootDoc).run();

        JSONCompare.compare("/fixtures/responsemessagesorting/responsemessagesortingdesc.json", recorderMock);
    }

}
