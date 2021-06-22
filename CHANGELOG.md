
## 2.0.0-SNAPSHOT

* Supports OpenAPI 3.0
* Replaced `-apiBasePath`, `-schemes` with `-servers` to conform to the [specification on `server](https://swagger.io/specification/#server-object)
* Removed support for `@resourcePath` and `-resourceRootPath`
* Replaced `-apiAuthorizationsFile` with `-securitySchemesFile` representing a map of [Security Scheme Objects](https://swagger.io/specification/#security-scheme-object)
* Removed the support for `-extraApiDeclarations`

## 1.1.4

* Support for Spring MVC annotations

## 1.1.3

**Note from 1.1.3 and 1.0.7 the package and group id have changed from com.carma to com.tenxerconsulting.**

## 1.0.7 

**Note from 1.1.3 and 1.0.7 the package and group id have changed from com.carma to com.tenxerconsulting.**

## 1.1.2

* Support annotations for excluding fields/classes/operations (Issue 134)
* Use / for empty api paths (courtesy of glucas) (Issue 131)
* Support -longTypePrefixes option (Issue 130)
* Support @allowableValues for listing enum vals (Issue 129)
* Support v1 jackson annotations (courtesy of e99majo) (Issue 122)
* Support calendar fields as date-time (Issue 119)
* Fix reading of -subTypesAnnotations setting (Issue 116)
* Exclude AsyncResponse from documented parameters (Issue 111)
* Ability to add additional (implicit) params (Issue 108)

## 1.0.6

* Fix incorrect detection of sub resource methods (also improves performance) (Issue 107)
* Fix readme in relation to resourceRootPath (Issue 102)
* Group multiple response messages with same http status together for compatibility with swagger ui/spec (Issue 97)
* Fix performance degradation (Issue 94)
* Fix incorrect detection of sub resource methods (also improves performance) (Issue 107)
* Upgrade swagger UI to 2.1.0; fixes:
   * Api ordering in the embedded UI (Issue 70)
   * Success code not being displayed (Issue 77)
* Adding support for arrays of enums -courtesy of @tandrup (Issue 93)

## 1.1.1

* Fix incorrect detection of sub resource methods (also improves performance) (Issue 107)
* Support new Java 8 Date Time types (Issue 104)
* Fix readme in relation to resourceRootPath (Issue 102)
* Group multiple response messages with same http status together for compatibility with swagger ui/spec (Issue 97)
* Fix performance degradation (Issue 94)

## 1.1.0

* Support Java 8 (Issue 83)
* Upgrade swagger UI to 2.1.0; fixes:
    * Api ordering in the embedded UI (Issue 70)
    * Success code not being displayed (Issue 77)
* Adding support for arrays of enums -courtesy of @tandrup (Issue 93)

## 1.0.5

* Added better support for subTypes - courtesy of @mhardorf (Issue 86)
* Class PathParam Variables Not Being Added as Required Parameters to JSON Output - big help from @nkoterba (Issue 74)
* Add support for data type format (Issue 84)
* Support not including private model fields by default via -defaultModelFieldsXmlAccessType flag (Issue 85)
* Support BigDecimal and BigInteger (Issue 87)
* Support allowable values javadoc tag (Issue 89)
* Update Documentation for Gradle Users in Resolving Models - courtesy of @nkoterba (Issue 82)
* Type identification does not work properly when mixing array types / regular types (Issue 81)
* Provide includeResourcePrefixes Configuration Option (Issue 80)
* @responseType doesn't support primitives (Issue 76)
* Issue Creating Paths with Regex Expressions - big help from @nkoterba (Issue 73)
* Model generation does not work properly for collections in some cases (Issue 72)
* Support for array types (Issue 71)
* @responseType ignored where method signature specifies generic return type (Issue 69)

## 1.0.4.2

* Classes referenced by a Collection but that do not occur as parameters are not included in the model (Issue 58)
* @Size on method parameter causes the model parsing to fail (Issue 57)

## 1.0.4.1

* fix a bug with @Size validation annotation on non numeric fields (Issue 53)

## 1.0.4

* Add support for javax.validation annotations (JSR-303) on DTOs (Issue 51)
* Doclet fails when destination directory does not exist (Issue 50)
* Support of HttpMethods PATCH/OPTIONS/HEAD (Issue 49)
* support wrapped types for paramaters e.g. Option < String > (Issue 47)
* support ordering response codes numerically (Issue 42)
* Workaround for Plugin fails when ${basedir} contains a space for apiInfoFile (Issue 41)
* abstract sub resources dont work (Issue 46)
* Doclet enters infinite loop when generic class parameter cannot be concretized (Issue 45)
* Resource with @Path("/") is ignored (Issue 44)
* NPE from missing model id when encountering @XmlTransient class (Issue 39)
* support generic JAXRS sub-resources (Issue 38)
* @Produces on class does not get propagated to method-level swagger docs (Issue 36)

## 1.0.3

* json view with nested jsonview annotated fields can cause duplicate model exception (Issue 35)
* Shipped swagger zip has nested directory (Issue 34)
* Ignore getters with parameter (Issue 33)
* Unable to set param as required when wrapped in @BeanParam (Issue 32)
* Support gradle (Issue 27)
* Support configuring json serialization/deserialization (Issue 26)

## 1.0.2

* Request body pojos not working with XmlAccessType.FIELD (Issue 25)
* @QueryParams are rendered as BODY type params when inside of a @BeanParam (Issue 31)
* Joda time classes are not handled meaning deep models are generated for them that the UI can't handle (Issue 24)
* Getters that return a different type to their field are not supported (portion of Issue 17)
* Getters/Setters without a corresponding field and which use a custom name are not supported (portion of Issue 17)
* Boolean getters that use the is* naming convention can result in duplicate fields (portion of Issue 17)
* Add option to disable use of @XmlAccessorType (portion of Issue 25)
* For @XmlAccessorType Support Public Member, None and allow jaxb annotated fields/members to override the default Field and Property behaviour (portion of Issue 25)
* Support Json Subtypes (Issue 22)
* Support project wide naming conventions like lowercase underscore separated for model fields (portion of Issue 17)

## 1.0.1

* custom response type not being added to model (issue 21)
* support api level descriptions (issue 19)
* @XmlAttribute name is not being used for model field names (issue 18)
* support variables in the javadoc which we can replace with values from a properties file (issue 14)
* Support relative basePath w/ port (issue 20)
* @XmlTransient or @JsonIgnore on setters can lead to invalid model fields (portion of issue 17)
