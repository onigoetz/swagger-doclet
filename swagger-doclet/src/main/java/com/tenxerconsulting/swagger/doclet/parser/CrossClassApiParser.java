package com.tenxerconsulting.swagger.doclet.parser;

import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.Tag;
import com.sun.javadoc.Type;
import com.tenxerconsulting.swagger.doclet.DocletOptions;
import com.tenxerconsulting.swagger.doclet.model.*;
import io.swagger.oas.models.*;
import io.swagger.oas.models.media.Schema;
import lombok.extern.slf4j.Slf4j;

/**
 * The CrossClassApiParser represents an api class parser that supports ApiDeclaration being
 * spread across multiple resource classes.
 *
 * @author conor.roche
 * @version $Id$
 */
@Slf4j
public class CrossClassApiParser {

    private final OpenAPI openapi;
    private final DocletOptions options;
    private final ClassDoc classDoc;
    private final Collection<ClassDoc> classes;
    private final String rootPath;

    private final Method parentMethod;
    private final List<ClassDoc> subResourceClasses;
    private final Collection<ClassDoc> typeClasses;
    private final List<TagWrapper> tags;

    private String classResourceTag = null;
    private String classResourcePriority = null;
    private String classResourceDescription = null;

    /**
     * This creates a CrossClassApiParser for top level parsing
     *
     * @param openapi            OpenAPI object
     * @param options            The options for parsing
     * @param classDoc           The class doc
     * @param classes            The doclet classes to document
     * @param typeClasses        Extra type classes that can be used as generic parameters
     * @param subResourceClasses Sub resource doclet classes
     * @param tags               List of TagWrappers
     */
    public CrossClassApiParser(OpenAPI openapi, DocletOptions options, ClassDoc classDoc, Collection<ClassDoc> classes, List<ClassDoc> subResourceClasses,
                               Collection<ClassDoc> typeClasses, List<TagWrapper> tags) {
        super();
        this.openapi = openapi;
        this.options = options;
        this.classDoc = classDoc;
        this.classes = classes;
        this.typeClasses = typeClasses;
        this.subResourceClasses = subResourceClasses;
        this.tags = tags;
        this.rootPath = ParserHelper.resolveClassPath(classDoc, options);
        this.parentMethod = null;
    }

    /**
     * This creates a CrossClassApiParser for parsing a subresource
     *
     * @param openapi            OpenAPI object
     * @param options            The options for parsing
     * @param classDoc           The class doc
     * @param classes            The doclet classes to document
     * @param typeClasses        Extra type classes that can be used as generic parameters
     * @param subResourceClasses Sub resource doclet classes
     * @param tags               List of tagWrappers
     * @param parentMethod       The parent method that "owns" this sub resource
     * @param parentPath         The parent path
     */
    public CrossClassApiParser(OpenAPI openapi, DocletOptions options, ClassDoc classDoc, Collection<ClassDoc> classes, List<ClassDoc> subResourceClasses,
                               Collection<ClassDoc> typeClasses, List<TagWrapper> tags, Method parentMethod, String parentPath, String classResourceTag,
                               String classResourcePriority, String classResourceDescription) {
        super();
        this.openapi = openapi;
        this.options = options;
        this.classDoc = classDoc;
        this.classes = classes;
        this.typeClasses = typeClasses;
        this.subResourceClasses = subResourceClasses;
        this.tags = tags;
        this.classResourceTag = classResourceTag;
        this.classResourcePriority = classResourcePriority;
        this.classResourceDescription = classResourceDescription;
        this.rootPath = parentPath + ParserHelper.resolveClassPath(classDoc, options);
        this.parentMethod = parentMethod;
    }

    static boolean stringIsNullOrEmpty(String string) {
        return string == null || string.isEmpty();
    }

    static String emptyToNull(String string) {
        return stringIsNullOrEmpty(string) ? null : string;
    }

    /**
     * This gets the root jaxrs path of the api resource class
     *
     * @return The root path
     */
    public String getRootPath() {
        return this.rootPath;
    }

    /**
     * This parses the api declarations from the resource classes of the api
     *
     * @param declarations The map of resource name to declaration which will be added to
     */
    public void parse(Paths declarations) {
        Collection<ClassDoc> allClasses = new ArrayList<>();
        allClasses.addAll(this.classes);
        allClasses.addAll(this.typeClasses);

        ClassDocCache classCache = new ClassDocCache(allClasses);

        // see if this is a resource class, it is if either it has class level @Path or has @GET etc on one of its methods
        // (sub resource classes don't have @Path but will have method annotations)
        if (this.rootPath.isEmpty()) {
            boolean methodFound = false;
            for (MethodDoc method : this.classDoc.methods()) {
                if (ParserHelper.resolveMethodHttpMethod(method, options) != null) {
                    methodFound = true;
                    break;
                }
            }
            if (!methodFound) {
                if (this.options.isLogDebug()) {
                    log.debug("Ignoring non resource class: {}", this.classDoc.name());
                }
                return;
            }
        }

        ClassDoc currentClassDoc = this.classDoc;
        while (currentClassDoc != null) {

            if (this.options.isLogDebug()) {
                log.debug("Processing resource class: {}", currentClassDoc.name());
            }

            // read default error type for class
            String defaultErrorTypeClass = ParserHelper.getInheritableTagValue(currentClassDoc, this.options.getDefaultErrorTypeTags(), this.options);
            Type defaultErrorType = ParserHelper.findModel(this.classes, defaultErrorTypeClass);

            Set<ModelWrapper> classModels = new HashSet<>();
            if (this.options.isParseModels() && defaultErrorType != null) {
                classModels.addAll(new ApiModelParser(this.options, this.options.getTranslator(), defaultErrorType, null, this.classes).parse());
            }

            // read class level resource tag, priority and description
            // if classResourceTag isn't null, then a subclass has already set the classResourceTag.
            if (this.classResourceTag == null) {
                this.classResourceTag = ParserHelper.getInheritableTagValue(currentClassDoc, this.options.getResourceTags(), this.options);
            }
            if (this.classResourcePriority == null) {
                this.classResourcePriority = ParserHelper.getInheritableTagValue(currentClassDoc, this.options.getResourcePriorityTags(), this.options);
            }
            if (this.classResourceDescription == null) {
                this.classResourceDescription = ParserHelper.getInheritableTagValue(currentClassDoc, this.options.getResourceDescriptionTags(), this.options);
            }

            // check if its a sub resource
            boolean isSubResourceClass = this.subResourceClasses != null && this.subResourceClasses.contains(currentClassDoc);

            // dont process a subresource outside the context of its parent method
            if (isSubResourceClass && this.parentMethod == null) {
                // skip
                if (this.options.isLogDebug()) {
                    log.debug("Skipping class as its a sub resource class and we are outside of the parent method context.");
                }
            } else {
                for (MethodDoc method : currentClassDoc.methods()) {

                    if (this.options.isLogDebug()) {
                        log.debug("Processing method: {}", method.name());
                    }

                    ApiMethodParser methodParser = this.parentMethod == null ? new ApiMethodParser(this.options, this.rootPath, method, allClasses,
                            defaultErrorTypeClass) : new ApiMethodParser(this.options, this.parentMethod, method, allClasses, defaultErrorTypeClass);

                    Method parsedMethod = methodParser.parse();
                    if (parsedMethod == null) {
                        if (this.options.isLogDebug()) {
                            log.debug("skipping method: {} as it was not parsed to an api method", method.name());
                        }
                        continue;
                    }

                    // see which resource tag to use for the method, if its got a resourceTag then use that
                    // otherwise use the classResourceTag, falling back to the root path
                    String resourceTag = getResourceTag(this.classResourceTag, method);

                    // look for a priority tag for the resource listing and set on the resource if the resource hasn't had one set
                    int tagPriority = getTagPriority(this.classResourcePriority, method);

                    // look for a method level description tag for the resource listing and set on the resource if the resource hasn't had one set
                    String tagDescription = getTagDescription(this.classResourceDescription, method);

                    if (parsedMethod.isSubResource()) {
                        if (this.options.isLogDebug()) {
                            log.debug("parsing method: {} as a subresource", method.name());
                        }
                        ClassDoc subResourceClassDoc = classCache.findByType(method.returnType());
                        // look for a custom return type, this is useful where we return a jaxrs Resource in the method signature
                        // which typically returns a different subResource object
                        if (subResourceClassDoc == null) {
                            Type customType = ParserHelper.readCustomType(method, this.options, this.classes);
                            subResourceClassDoc = classCache.findByType(customType);
                        }
                        if (subResourceClassDoc != null) {
                            // delete class from the dictionary to handle recursive sub-resources
                            Collection<ClassDoc> shrunkClasses = new ArrayList<ClassDoc>(this.classes);
                            shrunkClasses.remove(currentClassDoc);
                            // recursively parse the sub-resource class
                            CrossClassApiParser subResourceParser = new CrossClassApiParser(this.openapi, this.options, subResourceClassDoc, shrunkClasses,
                                    this.subResourceClasses, this.typeClasses, this.tags, parsedMethod, parsedMethod.getPath(), resourceTag,
                                    String.valueOf(tagPriority), tagDescription);
                            subResourceParser.parse(declarations);
                        }
                        continue;
                    }

                    PathItem path = declarations.get(parsedMethod.getPath());
                    if (path == null) {
                        path = new PathItem();
                        declarations.put(parsedMethod.getPath(), path);
                        if (this.options.isLogDebug()) {
                            log.debug("Creating new api path for method: {}", method.name());
                        }
                    } else {
                        if (this.options.isLogDebug()) {
                            log.debug("Reusing api declaration ({}) for method: {}", parsedMethod.getPath(), method.name());
                        }
                    }

                    // find api this method should be added to
                    addOperation(parsedMethod, path, resourceTag);

                    // add Tag to Swagger object. This is referenced in the Operation Object and is used to layout the apis
                    io.swagger.oas.models.tags.Tag tag = new io.swagger.oas.models.tags.Tag();
                    tag.name(resourceTag);
                    tag.description(tagDescription);
                    TagWrapper tagWrapper = new TagWrapper(tag, tagPriority);

                    if (tags.stream().noneMatch(t -> tag.getName().equals(t.getTag().getName()))) {
                        // TODO :: merge tags if they already exists, might get a missing description
                        this.tags.add(tagWrapper);
                    }

                    // add models
                    Set<ModelWrapper> methodModels = methodParser.models();

                    for (Map.Entry<String, ModelWrapper> entry : addApiModels(classModels, methodModels, method).entrySet()) {
                        log.info("Adding model: {}", entry.getKey());
                        if (this.options.isLogDebug()) {
                            Map<String, Schema> schemas = openapi.getComponents().getSchemas();
                            if (schemas != null && schemas.containsKey(entry.getKey())) {
                                log.warn("model with name {} already exists. Overwriting previous model", entry.getKey());
                            }
                        }

                        if (openapi.getComponents() == null) {
                            openapi.components(new Components());
                        }

                        openapi.getComponents().addSchemas(
                                entry.getKey(),
                                entry.getValue().getSchema()
                        );
                    }

                    if (this.options.isLogDebug()) {
                        log.debug("Finished processing for method: {}", method.name());
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

    private String getResourceTag(String classResourceTag, MethodDoc method) {
        String resourceTag = getRootPath();
        if (classResourceTag != null) {
            resourceTag = classResourceTag;
        }

        if (this.options.getResourceTags() != null) {
            for (String javadocResourceTag : this.options.getResourceTags()) {
                Tag[] resourceTags = method.tags(javadocResourceTag);
                if (resourceTags != null && resourceTags.length > 0) {
                    resourceTag = resourceTags[0].text();
                    resourceTag = resourceTag.trim().replace(" ", "_");
                    break;
                }
            }
        }

        // sanitize the path and ensure it starts with /
        if (resourceTag != null) {
            resourceTag = ParserHelper.sanitizePath(resourceTag);
        }

        return resourceTag;
    }

    private Map<String, ModelWrapper> addApiModels(Set<ModelWrapper> classModels, Set<ModelWrapper> methodModels, MethodDoc method) {
        methodModels.addAll(classModels);

        try {
            return methodModels.stream()
                    .filter(model ->
                            openapi.getComponents() == null ||
                                    !openapi.getComponents()
                                            .getSchemas()
                                            .containsKey(model.getName())
                    )
                    .collect(Collectors.toMap(ModelWrapper::getName, model -> model));
        } catch (Exception ex) {
            StringBuilder stringifiedModels = new StringBuilder();
            int num = 0;
            for (ModelWrapper wrapper : methodModels) {
                num++;
                stringifiedModels.append("\n\n" + num + ":\n");
                try {
                    stringifiedModels.append(options.getMapper().writeValueAsString(wrapper));
                } catch (JsonProcessingException e) {
                    stringifiedModels.append(wrapper.toString());
                }
            }

            throw new IllegalStateException(
                    "Detected duplicate models, if you use classes with the same name from different packages please set the doclet option -useFullModelIds and retry. The problematic method was : "
                            + method + ", and models were: " + stringifiedModels, ex);
        }
    }

    private int getTagPriority(String classResourcePriority, MethodDoc method) {
        int priorityVal = 0;
        String priority = ParserHelper.getInheritableTagValue(method, this.options.getResourcePriorityTags(), this.options);
        if (priority != null) {
            priorityVal = Integer.parseInt(priority);
        } else if (classResourcePriority != null) {
            // set from the class
            priorityVal = Integer.parseInt(classResourcePriority);
        }

        return priorityVal;
    }

    private String getTagDescription(String classResourceDescription, MethodDoc method) {
        String description = ParserHelper.getInheritableTagValue(method, this.options.getResourceDescriptionTags(), this.options);
        if (description == null) {
            description = classResourceDescription;
        }
        return description;
    }

    private void addOperation(Method method, PathItem path, String tag) {
        io.swagger.oas.models.Operation operation = new io.swagger.oas.models.Operation();

        operation.setOperationId(emptyToNull(method.getMethodName()));
        operation.setRequestBody(method.getRequestBody());
        operation.setResponses(method.getResponses());
        operation.setTags(Collections.singletonList(tag));

        operation.setParameters(method.getApiParameters().isEmpty() ? null : method.getApiParameters());

        operation.setSummary(emptyToNull(method.getSummary()));
        operation.setDescription(emptyToNull(method.getDescription()));
        operation.setSecurity(method.getSecurity());

        if (method.isDeprecated()) {
            operation.deprecated(method.isDeprecated());
        }

        PathItem.HttpMethod httpMethod = parentMethod != null && parentMethod.getMethod() != null
                ? parentMethod.getMethod().getOpenapiMethod()
                : method.getMethod().getOpenapiMethod();

        if (path.readOperationsMap().containsKey(httpMethod) && !path.readOperationsMap().get(httpMethod).getOperationId().equals(method.getMethodName())) {
            log.error("An operation for '" + httpMethod.name() + " " + method.getPath() + "' already exists. Replacing '" + path.readOperationsMap().get(httpMethod).getOperationId() + "' with '" + method.getMethodName() + "'");
        }

        path.operation(httpMethod, operation);
    }

}
