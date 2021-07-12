package com.tenxerconsulting.swagger.doclet.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Type;

import com.tenxerconsulting.swagger.doclet.model.*;
import com.tenxerconsulting.swagger.doclet.model.HttpMethod;
import com.tenxerconsulting.swagger.doclet.DocletOptions;
import com.tenxerconsulting.swagger.doclet.Recorder;
import com.tenxerconsulting.swagger.doclet.ServiceDoclet;
import io.swagger.oas.models.*;
import io.swagger.oas.models.info.Info;
import io.swagger.oas.models.servers.Server;
import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("javadoc")
@Slf4j
public class JaxRsAnnotationParser {

    // https://spec.openapis.org/oas/v3.0.0
    private static final String SWAGGER_VERSION = "3.0";

    // TODO :: update to 3.50 or more recent
    private static final String SWAGGER_UI_VERSION = "2.2.10";

    private final DocletOptions options;
    private final RootDoc rootDoc;

    private static final <T> void addIfNotNull(Collection<T> collection, T item) {
        if (item != null) {
            collection.add(item);
        }
    }

    public JaxRsAnnotationParser(DocletOptions options, RootDoc rootDoc) {
        this.options = options;
        this.rootDoc = rootDoc;
    }

    public boolean run() {
        try {

            // setup additional classes needed for processing, generally these are java ones such as java.lang.String
            // adding them here allows them to be used in @outputType
            Collection<ClassDoc> typeClasses = new ArrayList<>();
            addIfNotNull(typeClasses, this.rootDoc.classNamed(java.lang.String.class.getName()));
            addIfNotNull(typeClasses, this.rootDoc.classNamed(java.lang.Integer.class.getName()));
            addIfNotNull(typeClasses, this.rootDoc.classNamed(java.lang.Boolean.class.getName()));
            addIfNotNull(typeClasses, this.rootDoc.classNamed(java.lang.Float.class.getName()));
            addIfNotNull(typeClasses, this.rootDoc.classNamed(java.lang.Double.class.getName()));
            addIfNotNull(typeClasses, this.rootDoc.classNamed(java.lang.Character.class.getName()));
            addIfNotNull(typeClasses, this.rootDoc.classNamed(java.lang.Long.class.getName()));
            addIfNotNull(typeClasses, this.rootDoc.classNamed(java.lang.Byte.class.getName()));
            addIfNotNull(typeClasses, this.rootDoc.classNamed(java.util.Date.class.getName()));
            addIfNotNull(typeClasses, this.rootDoc.classNamed(java.util.Calendar.class.getName()));
            addIfNotNull(typeClasses, this.rootDoc.classNamed(java.util.Map.class.getName()));
            addIfNotNull(typeClasses, this.rootDoc.classNamed(java.util.Collection.class.getName()));
            addIfNotNull(typeClasses, this.rootDoc.classNamed(java.util.Set.class.getName()));
            addIfNotNull(typeClasses, this.rootDoc.classNamed(java.util.List.class.getName()));
            addIfNotNull(typeClasses, this.rootDoc.classNamed(java.math.BigInteger.class.getName()));
            addIfNotNull(typeClasses, this.rootDoc.classNamed(java.math.BigDecimal.class.getName()));
            addIfNotNull(typeClasses, this.rootDoc.classNamed(java.util.UUID.class.getName()));
            addIfNotNull(typeClasses, this.rootDoc.classNamed(java.time.DayOfWeek.class.getName()));
            addIfNotNull(typeClasses, this.rootDoc.classNamed(java.time.Duration.class.getName()));
            addIfNotNull(typeClasses, this.rootDoc.classNamed(java.time.Instant.class.getName()));
            addIfNotNull(typeClasses, this.rootDoc.classNamed(java.time.LocalDate.class.getName()));
            addIfNotNull(typeClasses, this.rootDoc.classNamed(java.time.LocalDateTime.class.getName()));
            addIfNotNull(typeClasses, this.rootDoc.classNamed(java.time.Month.class.getName()));
            addIfNotNull(typeClasses, this.rootDoc.classNamed(java.time.MonthDay.class.getName()));
            addIfNotNull(typeClasses, this.rootDoc.classNamed(java.time.OffsetDateTime.class.getName()));
            addIfNotNull(typeClasses, this.rootDoc.classNamed(java.time.OffsetTime.class.getName()));
            addIfNotNull(typeClasses, this.rootDoc.classNamed(java.time.Period.class.getName()));
            addIfNotNull(typeClasses, this.rootDoc.classNamed(java.time.Year.class.getName()));
            addIfNotNull(typeClasses, this.rootDoc.classNamed(java.time.YearMonth.class.getName()));
            addIfNotNull(typeClasses, this.rootDoc.classNamed(java.time.ZoneId.class.getName()));
            addIfNotNull(typeClasses, this.rootDoc.classNamed(java.time.ZoneOffset.class.getName()));
            addIfNotNull(typeClasses, this.rootDoc.classNamed(java.time.ZonedDateTime.class.getName()));
            addIfNotNull(typeClasses, this.rootDoc.classNamed(java.net.URI.class.getName()));
            addIfNotNull(typeClasses, this.rootDoc.classNamed(java.net.URL.class.getName()));

            // filter the classes to process
            Collection<ClassDoc> docletClasses = new ArrayList<>();
            for (ClassDoc classDoc : this.rootDoc.classes()) {

                // see if excluded via its FQN
                boolean excludeResource = false;
                if (this.options.getExcludeResourcePrefixes() != null && !this.options.getExcludeResourcePrefixes().isEmpty()) {
                    for (String prefix : this.options.getExcludeResourcePrefixes()) {

                        String className = classDoc.qualifiedName();
                        if (className.startsWith(prefix)) {
                            excludeResource = true;
                            break;
                        }
                    }
                }

                // see if the inclusion filter is set and if so this resource must match this
                if (!excludeResource && this.options.getIncludeResourcePrefixes() != null && !this.options.getIncludeResourcePrefixes().isEmpty()) {
                    boolean matched = false;
                    for (String prefix : this.options.getIncludeResourcePrefixes()) {
                        String className = classDoc.qualifiedName();
                        if (className.startsWith(prefix)) {
                            matched = true;
                            break;
                        }
                    }
                    excludeResource = !matched;
                }

                if (excludeResource) {
                    continue;
                }

                // see if deprecated
                if (this.options.isExcludeDeprecatedResourceClasses() && ParserHelper.isDeprecated(classDoc, this.options)) {
                    continue;
                }

                // see if excluded via a tag or annotation
                if (ParserHelper.hasTag(classDoc, this.options.getExcludeClassTags())) {
                    continue;
                }
                if (ParserHelper.hasAnnotation(classDoc, this.options.getExcludeClassAnnotations(), this.options)) {
                    continue;
                }

                docletClasses.add(classDoc);
            }

            ClassDocCache classCache = new ClassDocCache(docletClasses);
            io.swagger.oas.models.OpenAPI swagger = new OpenAPI();
            swagger.setOpenapi(SWAGGER_VERSION);

            List<TagWrapper> tags = new ArrayList<>();
            Paths declarations = null;

            // build up set of subresources
            // do simple parsing to find sub resource classes
            // these are ones referenced in the return types of methods
            // which have a path but no http method
            // or classes w/o a class level @Path
            List<ClassDoc> subResourceClasses = new ArrayList<>();
            for (ClassDoc classDoc : docletClasses) {
                if (ParserHelper.resolveClassPath(classDoc, this.options).isEmpty()) {
                    for (MethodDoc method : classDoc.methods()) {
                        if (ParserHelper.resolveMethodHttpMethod(method, options) != null) {
                            if (!subResourceClasses.contains(classDoc)) {
                                if (this.options.isLogDebug()) {
                                    log.debug("Adding return type as sub resource class : {}. This resource class is missing a class level @Path", classDoc.name());
                                }

                                subResourceClasses.add(classDoc);
                            }
                            break;
                        }
                    }

                    continue;
                }

                ClassDoc currentClassDoc = classDoc;
                while (currentClassDoc != null) {

                    for (MethodDoc method : currentClassDoc.methods()) {
                        // if the method has @Path but no Http method then its an entry point to a sub resource
                        if (!ParserHelper.resolveMethodPath(method, this.options).isEmpty() && HttpMethod.fromMethod(method, options) == null) {
                            String responseTypeTag = ParserHelper.responseTypeTagOf(method, this.options);
                            ClassDoc subResourceClassDoc = responseTypeTag == null ? classCache.findByType(method.returnType()) : classCache.findByName(responseTypeTag);
                            // look for a custom return type, this is useful where we return a jaxrs Resource in the method signature
                            // which typically returns a different subResource object
                            if (subResourceClassDoc == null) {
                                Type customType = ParserHelper.readCustomType(method, this.options, docletClasses);
                                subResourceClassDoc = classCache.findByType(customType);
                            }
                            if (subResourceClassDoc != null) {
                                if (!subResourceClasses.contains(subResourceClassDoc)) {
                                    if (this.options.isLogDebug()) {
                                        log.debug("Adding return type as sub resource class : {} for method {} of referencing class {}", subResourceClassDoc.name(), method.name(), currentClassDoc.name());
                                    }

                                    subResourceClasses.add(subResourceClassDoc);
                                }
                            }
                        }
                    }

                    currentClassDoc = currentClassDoc.superclass();

                    // ignore parent object class
                    if (!ParserHelper.hasAncestor(currentClassDoc)) {
                        break;
                    }
                }
            }

            // parse with the v2 parser that supports endpoints of the same resource being spread across resource files
            Paths resourceToDeclaration = new Paths();
            for (ClassDoc classDoc : docletClasses) {
                CrossClassApiParser classParser = new CrossClassApiParser(swagger, this.options, classDoc, docletClasses, subResourceClasses, typeClasses,
                        tags);
                classParser.parse(resourceToDeclaration);
            }

            if (this.options.isLogDebug()) {
                log.debug("After parse phase api declarations are: ");
                for (Map.Entry<String, PathItem> entry : resourceToDeclaration.entrySet()) {
                    log.debug("Api Dec: path {}", entry.getKey());
                    for (Map.Entry<PathItem.HttpMethod, Operation> opEntry : entry.getValue().readOperationsMap().entrySet()) {
                        log.debug("Api nick name: {} method  {}", opEntry.getValue().getOperationId(), opEntry.getKey().toString());
                    }
                }
            }

            // set root path on any empty resources
            List<TagWrapper> tagsToRemove = new ArrayList<>();
            for (TagWrapper tagWrapper : tags) {
                if (tagWrapper.getTag().getName().equals("/")) {
                    tagsToRemove.add(tagWrapper);
                }
            }

            tags.removeAll(tagsToRemove);

            // merge the api declarations
//			declarationColl = new ApiDeclarationMerger(SWAGGER_VERSION, this.options.getApiVersion(), this.options.getApiBasePath()).merge(declarationColl);

            declarations = new Paths();

            List<TagWrapper> sortedTags = new LinkedList<>(tags);
            List<Map.Entry<String, PathItem>> sortedDeclarations = new LinkedList<>(resourceToDeclaration.entrySet());
            // sort the api declarations if needed
            if (this.options.isSortResourcesByPriority()) {

                sortedTags.sort(Comparator.comparingInt(TagWrapper::getPriority));

            } else if (this.options.isSortResourcesByPath()) {
                sortedTags.sort((t1, t2) -> {
                    if (t1 == null || t1.getTag().getName() == null) {
                        return -1;
                    }
                    return t1.getTag().getName().compareTo(t2.getTag().getName());
                });
            }

            // sort apis of each declaration
            if (this.options.isSortApisByPath()) {
                sortedDeclarations.sort(Map.Entry.comparingByKey());
            }

            for (Map.Entry<String, PathItem> entry : sortedDeclarations) {
                declarations.put(entry.getKey(), entry.getValue());
            }

            for (TagWrapper tag : sortedTags) {
                swagger.addTagsItem(tag.getTag());
            }

            if (null == this.options.getApiInfo() && null != this.options.getApiVersion()) {
                this.options.setApiInfo(new Info());
                this.options.getApiInfo().setVersion(this.options.getApiVersion());
            }

            swagger.paths(declarations);

            for (String s : this.options.getServers()) {
                Server server = new Server();
                server.url(s);
                swagger.addServersItem(server);
            }

            swagger.setInfo(this.options.getApiInfo());

            if (options.getSecuritySchemes() != null) {
                if (swagger.getComponents() == null) {
                    swagger.setComponents(new Components());
                }

                swagger.getComponents().setSecuritySchemes(options.getSecuritySchemes());
            }

            writeApis(swagger);
            // Copy swagger-ui into the output directory.
            if (this.options.isIncludeSwaggerUi()) {
                copyUi();
            }
            return true;
        } catch (IOException e) {
            log.error("Failed to write api docs, err msg: " + e.getMessage(), e);
            return false;
        }
    }

    private void writeApis(OpenAPI openapi) throws IOException {

        File outputDirectory = this.options.getOutputDirectory();
        Recorder recorder = this.options.getRecorder();

        File docFile = new File(outputDirectory, "service.json");
        recorder.record(docFile, openapi);

    }

    private void copyUi() throws IOException {
        File outputDirectory = this.options.getOutputDirectory();
        if (outputDirectory == null) {
            outputDirectory = new File(".");
        }
        Recorder recorder = this.options.getRecorder();
        String uiPath = this.options.getSwaggerUiPath();

        if (uiPath == null) {
            // default inbuilt zip
            copyZip(recorder, null, outputDirectory);
        } else {
            // zip or dir
            File uiPathFile = new File(uiPath);
            if (uiPathFile.isDirectory()) {
                log.info("Using swagger dir from: {}", uiPathFile.getAbsolutePath());
                copyDirectory(recorder, uiPathFile, uiPathFile, outputDirectory);
            } else if (!uiPathFile.exists()) {
                File f = new File(".");
                log.info("SwaggerDoclet working directory: {}", f.getAbsolutePath());
                log.info("-swaggerUiPath not set correctly as it did not exist: {}", uiPathFile.getAbsolutePath());
                throw new RuntimeException("-swaggerUiPath not set correctly as it did not exist: " + uiPathFile.getAbsolutePath());
            } else {
                copyZip(recorder, uiPathFile, outputDirectory);
            }
        }
    }

    private void copyZip(Recorder recorder, File uiPathFile, File outputDirectory) throws IOException {
        ZipInputStream swaggerZip = null;
        try {
            if (uiPathFile == null) {
                swaggerZip = new ZipInputStream(ServiceDoclet.class.getResourceAsStream("/swagger-ui-" + SWAGGER_UI_VERSION + ".zip"));
                log.info("Using default swagger-ui.zip file from SwaggerDoclet jar file");
            } else {
                swaggerZip = new ZipInputStream(new FileInputStream(uiPathFile));
                log.info("Using swagger-ui.zip file from: {}", uiPathFile.getAbsolutePath());
            }

            ZipEntry entry = swaggerZip.getNextEntry();
            while (entry != null) {
                final File swaggerFile = new File(outputDirectory, entry.getName());
                if (entry.isDirectory()) {
                    if (!swaggerFile.isDirectory() && !swaggerFile.mkdirs()) {
                        throw new RuntimeException("Unable to create directory: " + swaggerFile);
                    }
                } else {
                    try (FileOutputStream outputStream = new FileOutputStream(swaggerFile)) {
                        copy(swaggerZip, outputStream);
                        outputStream.flush();
                    }
                }

                entry = swaggerZip.getNextEntry();
            }

        } finally {
            if (swaggerZip != null) {
                swaggerZip.close();
            }
        }
    }

    private void copy(InputStream source, OutputStream target) throws IOException {
        byte[] buf = new byte[8192];
        int length;
        while ((length = source.read(buf)) > 0) {
            target.write(buf, 0, length);
        }
    }

    private void copyDirectory(Recorder recorder, File uiPathFile, File sourceLocation, File targetLocation) throws IOException {
        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists() && !targetLocation.mkdirs()) {
                throw new IOException("Failed to create the dir: " + targetLocation.getAbsolutePath());
            }

            String[] children = sourceLocation.list();
            if (children != null) {
                for (String element : children) {
                    copyDirectory(recorder, uiPathFile, new File(sourceLocation, element), new File(targetLocation, element));
                }
            }
        } else {
            try (InputStream in = new FileInputStream(sourceLocation); OutputStream out = new FileOutputStream(targetLocation);) {
                copy(in, out);
                out.flush();
            }
        }
    }

}
