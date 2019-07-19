package fr.milleis.morphit.processors;

import fr.milleis.morphit.annotations.converter.Converter;
import fr.milleis.morphit.annotations.converter.ConverterMethods;
import fr.milleis.morphit.annotations.field.MorphField;
import fr.milleis.morphit.annotations.method.MorphMethod;
import fr.milleis.morphit.annotations.method.MorphMethodMirror;
import fr.milleis.morphit.annotations.field.Names;
import fr.milleis.morphit.annotations.field.Types;
import fr.milleis.morphit.utils.JavaLang;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.JavaFileObject;

/**
 * HAVE TO BE CLEANED
 *
 * @author ajosse
 */
public abstract class MorphProcessor extends AbstractProcessor {

    private static final String PACKAGE = "package";
    private static final String END = ";";
    private static final String CLOSE = ")";
    private static final String OPEN = "(";
    private static final String SET = "set";
    private static final String CALL = "()";
    private static final String DOT = ".";
    private static final String GET = "get";
    private static final String BUILD = "build";
    private static final String CURRENT = "p";
    private static final String BUILDER = "builder";
    private static final String IMPORT = "import";
    private static final String SPACE = " ";
    private static final String CLOSE_SCOPE = "}";
    private static final String OPEN_SCOPE = "{";
    private static final String EXTENDS = "extends";
    private static final String PUBLIC = "public";
    private static final String CLASS = "class";
    private static final String IMPLEMENTS = "implements";
    private static final String IS = "is";

    private static final String RETURN = "return";

    private static final String NEW = "new";
    private static final String EQUAL = "=";
    private static final String CLOSE_STRIPE = ">";
    private static final String FOR = "for";
    private static final String ARRAY_LIST = "ArrayList";
    private static final String OPEN_STRIPE = "<";
    private static final String ADD = "add";
    private static final String DOUBLE_DOT = ":";
    private static final String ITERATOR = "it";
    private static final String OVERRIDE = "@Override";
    private static final String IMPL = "Impl";
    private static final String JAVAUTIL_ARRAY_LIST = "java.util.ArrayList;";
    private static final String SUPER = "super";
    private static final String IMPORT_ORGSPRINGFRAMEWORKBEANSFACTORYANNO = "import org.springframework.beans.factory.annotation.Autowired;";
    private static final String NULL = "null";
    private static final String NOT_EQUALS = "!=";
    private static final String IF = "if";
    private static final String MORPH_BUILDER = "MorphBuilder";
    private static final String IMPORT_FRMILLEISMORPHITBUILDER_MORPH_BUILDE = "import fr.milleis.morphit.builder.MorphBuilder;";
    private static final String COLLECTORSTO_LIST = "Collectors.toList";
    private static final String COLLECT = "collect";
    private static final String LAMBDA_ARROW = "->";
    private static final String MAP = "map";
    private static final String STREAM = "stream";
    private static final String IMPORT_JAVAUTILSTREAM_COLLECTORS = "import java.util.stream.Collectors;";

    private final ArrayList<String> imports = new ArrayList<>();
    private final ArrayList<Method> implemented = new ArrayList<>();
    private final ArrayList<Class> autowired = new ArrayList<>();

    private Class extension = null;
    private PrintWriter out;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Elements elementUtils = this.processingEnv.getElementUtils();
        for (TypeElement annotation : annotations) {
            Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);
            for (Element annotatedElement : annotatedElements) {
                PackageElement packageOf = elementUtils.getPackageOf(annotatedElement);

                try {
                    flush();
                    writeFile(
                            packageOf.getQualifiedName().toString(),
                            Class.forName(packageOf.getQualifiedName() + DOT + annotatedElement.getSimpleName())
                    );

                } catch (ClassNotFoundException | IOException ex) {
                    System.err.println(ex.getMessage());
                }
            }
        }

        return true;
    }

    private void flush() {
        imports.clear();
        implemented.clear();
        autowired.clear();
        extension = null;
    }

    /**
     * Looking for the first extension that has Impl class
     *
     * @param forName
     */
    private void handleExtension(Class<?> forName) {
        Class<?>[] interfaces = forName.getInterfaces();
        if (interfaces.length > 0) {
            for (Class<?> aInterface : interfaces) {
                if (aInterface != Object.class) {
                    //test if Impl class exist
                    try {
                        Class<?> forName1 = Class.forName(aInterface.getCanonicalName() + IMPL);
                        extension = forName1;
                        return;
                    } catch (ClassNotFoundException classNotFoundException) {
                        extension = null;
                    }
                }
            }

        }
    }

    private void writeFile(String packageName, Class classTarget) throws IOException {
        String className = classTarget.getSimpleName() + IMPL;

        JavaFileObject builderFile = processingEnv.getFiler()
                .createSourceFile(packageName + DOT + className);

        this.out = new PrintWriter(builderFile.openWriter());

        handleExtension(classTarget);

        writePackage(packageName);
        writeImports(classTarget);
        writeClassAndConstructor(className, classTarget);
        checkImplementedMethods(classTarget);

        writeMethods(classTarget);
        writeMethodMirrors(classTarget);
        writeImplementedMethods();
        writeCloseClass();
        this.out.flush();
        this.out.close();
    }

    /**
     * Write all methods registered in Annotated element
     *
     * @param classTarget
     * @throws SecurityException
     */
    private void writeMethods(Class classTarget) throws SecurityException {
        for (Method method : classTarget.getMethods()) {
            //if already implemented , continue ; 
            if (implemented.contains(method)) continue;
            MorphMethod morphMethod = method.getAnnotation(MorphMethod.class);
            Class<?> returnType = method.getReturnType();
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (morphMethod == null) continue;
            if (!validateMethod(returnType, parameterTypes, method)) continue;
            Class<?> paramType = parameterTypes[0];
            String parent = toFirstLetterLower(returnType.getSimpleName());
            ArrayList<Field> fields = gatherFieldsMirror(returnType, morphMethod.value(), paramType);

            writeMethod(returnType, method, paramType, morphMethod, parent, fields);
        }
        //write implemented method to call super.call()

    }

    /**
     * Check if method have to be writed in file<br>
     * need one param and a return type
     *
     * @param returnType
     * @param parameterTypes
     * @param method
     * @return
     */
    private boolean validateMethod(Class<?> returnType, Class<?>[] parameterTypes, Method method) {
        if (returnType.equals(Object.class)) {
            return false;
        }
        return parameterTypes.length != 0;
    }

    private void writeImplementedMethods() throws SecurityException {
        for (Method method : implemented) {
            Class<?> returnType = method.getReturnType();
            Class<?>[] parameterTypes = method.getParameterTypes();

            if (!validateMethod(returnType, parameterTypes, method)) continue;
            Class<?> paramType = parameterTypes[0];

            out.println(OVERRIDE);
            out.println(PUBLIC + SPACE + returnType.getSimpleName() + SPACE + method.getName()
                    + OPEN + paramType.getSimpleName() + SPACE
                    + toFirstLetterLower(paramType.getSimpleName()) + CLOSE
                    + OPEN_SCOPE);
            out.println(RETURN + SPACE + SUPER + DOT + method.getName() + OPEN + toFirstLetterLower(paramType.getSimpleName()) + CLOSE + END);
            out.println(CLOSE_SCOPE);

        }
        //write implemented method to call super.call()

    }

    /**
     * write one Method
     *
     * @param returnType
     * @param method
     * @param paramType
     * @param morphMethod
     * @param parent
     * @param fields
     * @throws SecurityException
     */
    private void writeMethod(Class<?> returnType, Method method, Class<?> paramType, MorphMethod morphMethod, String parent, ArrayList<Field> fields) throws SecurityException {
        out.println(OVERRIDE);
        out.print(PUBLIC + SPACE + returnType.getSimpleName() + SPACE + method.getName() + OPEN + paramType.getSimpleName() + SPACE + CURRENT + CLOSE + OPEN_SCOPE);
        //return null if CURRENT is null
        out.println(IF + OPEN + CURRENT + EQUAL + EQUAL + NULL + CLOSE + RETURN + SPACE + NULL + END);
        //CREATE ALL ARRAYLSIT NESTED
//        createNestedArraylists(morphMethod, out);
        out.println();
        out.println(returnType.getSimpleName() + SPACE + parent + SPACE + EQUAL + SPACE + returnType.getSimpleName() + DOT + BUILDER + CALL);
        //WRITE BUILDER
        writeMethodForMorphFields(morphMethod.value(), out, CURRENT);

        writeMethodForSimpleFields(fields, out, CURRENT);
//        writeSimpleNesteds(morphMethod, out);
        appendNestedLists(morphMethod, out);
        //BUILD
        out.print(DOT + BUILD + CALL + END);

        //INJECT ALL ARRAYLIST 
        writeNestedInjection(morphMethod, out, parent);
        //RETURN
        out.println(RETURN + SPACE + parent + END);
        //CLOSE METHOD
        out.println();
        out.print(CLOSE_SCOPE);
    }

//    /**
//     * Write all sync field for MorphNested
//     *
//     * @param morphMethod
//     * @param out
//     * @throws SecurityException
//     */
//    private void writeSimpleNesteds(MorphMethod morphMethod, final PrintWriter out) throws SecurityException {
//        for (MorphNested nested : morphMethod.nesteds()) {
//            if (nested.list()) continue;
//            //GATHER ALL MIRRO SIMPLE FIELD IN NESTED
//            ArrayList<Field> gatherFieldsMirror = gatherFieldsMirror(nested.targetType(), nested.fields(), new MorphNested[]{nested}, nested.sourceType());
//
//            String prefix = CURRENT + DOT + GET + toFirstLetterUpper(nested.source()) + CALL;
//            out.println(DOT + nested.target() + OPEN + nested.targetType().getSimpleName() + DOT + BUILDER + CALL);
//
//            //WRITE ALL GATHERED SIMPLE FIELD IN NESTED
//            writeMethodForSimpleFields(gatherFieldsMirror, out, prefix);
//            //WRITE ALL EXPLICIT FIELD
//            writeMethodForMorphFields(nested.fields(), out, prefix);
//
//            out.print(DOT + BUILD + CALL + CLOSE);
//            out.println();
//        }
//    }
    /**
     * Append all nested list describe before ot final object
     *
     * @param morphMethod
     * @param out
     */
    private void appendNestedLists(MorphMethod morphMethod, final PrintWriter out) {
        for (MorphField n : morphMethod.value()) {
            if (!n.list()) continue;
            String outLine;
            if (!n.converter().type().equals(JavaLang.class))
                outLine = (CURRENT + DOT + GET + toFirstLetterUpper(n.names().value()) + CALL
                        + DOT + STREAM + CALL + DOT + MAP + OPEN + ITERATOR + LAMBDA_ARROW + MORPH_BUILDER + DOT
                        + GET + OPEN + n.converter().type().getSimpleName() + DOT + CLASS + CLOSE + DOT + n.converter().methods().value() + OPEN + ITERATOR + CLOSE + CLOSE
                        + DOT + COLLECT + OPEN + COLLECTORSTO_LIST + CALL + CLOSE);
            else outLine = (CURRENT + DOT + GET + toFirstLetterUpper(n.names().value()) + CALL);
            out.println(DOT + getTarget(n.names()) + OPEN + NEW + SPACE + ARRAY_LIST + OPEN + outLine + CLOSE + CLOSE);
        }
    }

    /**
     *
     * @param fields
     * @param out
     * @param prefix
     */
    private void writeMethodForSimpleFields(ArrayList<Field> fields, final PrintWriter out, String prefix) {
        //all fields not delcared but same with target
        fields.forEach((field) -> {
            String prefixMethod = field.getType().equals(boolean.class) ? IS : GET;
            out.println(DOT + field.getName() + OPEN + prefix + DOT + prefixMethod + toFirstLetterUpper(field.getName()) + CALL + CLOSE);
        });
    }

    /**
     * Write all sync for simple Morph Fields<br>
     * Converter are handled
     *
     * @param fields
     * @param out
     * @param prefix
     */
    private void writeMethodForMorphFields(MorphField[] fields, final PrintWriter out, String prefix) {
        //all fields declared in annotations
        for (MorphField field : fields) {
            if (field.list()) continue;
            if (getTarget(field.names()).contains(DOT)) continue;
            String contextPrefix = prefix;
            String prefixMethod = field.isPrimitiveBoolean() ? IS : GET;
            boolean isPathedField = field.names().value().contains(DOT);
            if (field.names().value().contains(DOT)) {
                contextPrefix += addNestedPaths(field.names().value(), prefixMethod);
            }
            String converter = "";
            String converterEnd = "";
            if (!field.converter().type().equals(JavaLang.class)) {
                if (field.converter().morph()) {
                    converter = MORPH_BUILDER + DOT + GET + OPEN + field.converter().type().getSimpleName() + DOT + CLASS + CLOSE + DOT + field.converter().methods().value() + OPEN;
                    converterEnd = CLOSE;
                } else {
                    // call static
                    String converterCall = field.converter().type().getSimpleName();
                    converter = converterCall + DOT + field.converter().methods().value() + OPEN;
                    converterEnd = CLOSE;
                }
            }
            if (!isPathedField) {
                out.println(DOT + getTarget(field.names()) + OPEN + converter + contextPrefix + DOT + prefixMethod + toFirstLetterUpper(field.names().value()) + CALL + CLOSE + converterEnd);
            } else out.println(DOT + getTarget(field.names()) + OPEN + converter + contextPrefix + CLOSE + converterEnd);
        }
    }

    /**
     * create prefix for path source
     *
     * @example 'nested.nested.id' => '.getNested().getNested().getId()'
     * @param path
     * @param prefixMethod
     * @return
     */
    private String addNestedPaths(String path, String prefixMethod) {
        String[] split = path.split(Pattern.quote(DOT));
        String ret = "";
        for (String string : split) {
            ret += DOT + prefixMethod + toFirstLetterUpper(string) + CALL;
        }
        return ret;
    }

    /**
     * Look for all field that are mirroring in source and target<br>
     * check if on nested register any field to not repeat him in builder call
     *
     * @param returnType
     * @param fieldsMorph
     * @param fieldNesteds
     * @param paramType
     * @return
     * @throws SecurityException
     */
    private ArrayList<Field> gatherFieldsMirror(Class<?> returnType, MorphField[] fieldsMorph, Class<?> paramType) throws SecurityException {
        ArrayList<Field> fields = new ArrayList<>();
        for (Field declaredField : returnType.getDeclaredFields()) {
            try {
                if (!Arrays.asList(fieldsMorph).stream().anyMatch(e -> e.names().value().equals(declaredField.getName()))) {
                    fields.add(paramType.getDeclaredField(declaredField.getName()));
                }

            } catch (NoSuchFieldException ex) {
            }
        }
        return fields;
    }

    private void writeCloseClass() {
        out.println();
        out.print(CLOSE_SCOPE);
    }

    private void writeClassAndConstructor(String className, Class<?> classTarget) {

        String extend = extension != null ? (SPACE + EXTENDS + SPACE + extension.getSimpleName()) : "";

        out.println(PUBLIC + SPACE + CLASS + SPACE + className + extend + SPACE + IMPLEMENTS + SPACE + classTarget.getSimpleName() + OPEN_SCOPE);

        out.println(PUBLIC + SPACE + className + CALL + OPEN_SCOPE + CLOSE_SCOPE);

    }

    private void writeImports(Class<?> classTarget) throws SecurityException {

        //ALL TYPE REGISTERING
        for (Method method : classTarget.getMethods()) {
            MorphMethod morphMethod = method.getAnnotation(MorphMethod.class);
            if (morphMethod == null) continue;

            addToImports(method.getReturnType().getCanonicalName());
            addToImports(method.getParameterTypes()[0].getCanonicalName());

            importNestedType(morphMethod);
            importFieldType(morphMethod);
        }

        //IMPORT ALL TYPES
        imports.forEach((aImport) -> {
            out.println(IMPORT + SPACE + aImport + END);
        });
        //IMPORT EXTENDS
        if (extension != null && !extension.equals(Object.class))
            out.println(IMPORT + SPACE + extension.getCanonicalName() + END);
        out.println(IMPORT + SPACE + classTarget.getCanonicalName() + END);
        out.println(IMPORT + SPACE + JAVAUTIL_ARRAY_LIST);
        out.println(IMPORT_FRMILLEISMORPHITBUILDER_MORPH_BUILDE);
        out.println(IMPORT_JAVAUTILSTREAM_COLLECTORS);
        if (!autowired.isEmpty()) {
            out.println(IMPORT_ORGSPRINGFRAMEWORKBEANSFACTORYANNO);

        }

    }

    private void importFieldType(MorphMethod morphMethod) {
        for (MorphField nested : morphMethod.value()) {
            addToImports(nested.types().value().getCanonicalName());
            addToImports(nested.types().target().getCanonicalName());
            addToImports(nested.converter().type().getCanonicalName());
        }
    }

    private void importNestedType(MorphMethod morphMethod) {
        for (MorphField field : morphMethod.value()) {
            addToImports(field.types().value().getCanonicalName());
            addToImports(field.types().target().getCanonicalName());
            if (!field.converter().type().equals(JavaLang.class))
                addToImports(field.converter().type().getCanonicalName());

        }
    }

    private void addToImports(String classImport) {
        if (!imports.contains(classImport) && !classImport.equals(JavaLang.class.getCanonicalName()))
            imports.add(classImport);
    }

    private void writePackage(String packageName) {
        out.print(PACKAGE + SPACE + packageName + END);
        out.println();
    }

    private void writeNestedInjection(MorphMethod morphMethod, PrintWriter out, String parentVariableName) {

        for (MorphField field : morphMethod.value()) {

            if (getTarget(field.names()).contains(DOT)) {
                String[] split = getTarget(field.names()).split(Pattern.quote(DOT));
                out.println(parentVariableName + DOT + SET + toFirstLetterUpper(split[0]) + OPEN + field.types().target().getSimpleName() + DOT + BUILDER + CALL + DOT + split[1] + OPEN + CURRENT + DOT + GET + toFirstLetterUpper(field.names().value()) + CALL + CLOSE + DOT + BUILD + CALL + CLOSE + END);
            }

        }

    }

    private String toFirstLetterUpper(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private String toFirstLetterLower(String simpleName) {
        return simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);
    }

    /**
     * check if extension's method already have methods in annotated class
     *
     * @param classTarget
     */
    private void checkImplementedMethods(Class classTarget) {
        if (extension == null) return;
        for (Method method : classTarget.getMethods()) {
            for (Method method1 : extension.getMethods()) {
                //if name and return type and parameters type are same
                if (method.getName().equals(method1.getName()) && method.getReturnType().equals(method1.getReturnType())) {
                    Class<?>[] p = method.getParameterTypes();
                    Class<?>[] p1 = method1.getParameterTypes();
                    if (p.length > 0 && p1.length > 0) {
                        if (p[0].equals(p1[0]))
                            implemented.add(method);
                    }

                }
            }
        }
    }

    private void writeMethodMirrors(Class classTarget) {
        for (Method method : classTarget.getMethods()) {
            try {
                //if already implemented , continue ;
                if (implemented.contains(method)) continue;
                MorphMethodMirror morphMethodMirror = method.getAnnotation(MorphMethodMirror.class);
                Class<?> returnType = method.getReturnType();
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (morphMethodMirror == null) continue;
                if (!validateMethod(returnType, parameterTypes, method)) continue;
                Class<?> paramType = parameterTypes[0];
                String parent = toFirstLetterLower(returnType.getSimpleName());
                Method m = classTarget.getMethod(morphMethodMirror.value(), returnType);

                MorphMethod morphMethod = m.getAnnotation(MorphMethod.class);

                MorphMethod newMorphMethod = createNewmorphMethod(morphMethod);

                ArrayList<Field> fields = gatherFieldsMirror(returnType, newMorphMethod.value(), paramType);

                writeMethod(returnType, method, paramType, newMorphMethod, parent, fields);
            } catch (NoSuchMethodException | SecurityException ex) {
                ex.printStackTrace();
            }
        }
    }

    private MorphMethod createNewmorphMethod(MorphMethod reference) {
        MorphMethod newMorphMethod = new MorphMethod() {
            @Override
            public MorphField[] value() {
                final MorphField[] morphFields = new MorphField[reference.value().length];
                int i = 0;
                for (MorphField morphField : reference.value()) {
                    final MorphField morphField1 = new MorphField() {
                        @Override
                        public Names names() {
                            return new Names() {
                                @Override
                                public String value() {
                                    return getTarget(morphField.names());
                                }

                                @Override
                                public String target() {
                                    return morphField.names().value();
                                }

                                @Override
                                public Class<? extends Annotation> annotationType() {
                                    return morphField.names().annotationType();
                                }
                            };
                        }

                        @Override
                        public Types types() {
                            return new Types() {
                                @Override
                                public Class value() {
                                    return morphField.types().target();
                                }

                                @Override
                                public Class target() {
                                    return morphField.types().value();
                                }

                                @Override
                                public Class<? extends Annotation> annotationType() {
                                    return morphField.types().annotationType();
                                }
                            };
                        }

                        @Override
                        public Converter converter() {
                            return new Converter() {
                                @Override
                                public ConverterMethods methods() {
                                    return new ConverterMethods() {
                                        @Override
                                        public String value() {
                                            return morphField.converter().methods().mirror().equals("") ? morphField.converter().methods().value() : morphField.converter().methods().mirror();
                                        }

                                        @Override
                                        public String mirror() {
                                            return morphField.converter().methods().value();
                                        }

                                        @Override
                                        public Class<? extends Annotation> annotationType() {
                                            return morphField.converter().annotationType();
                                        }
                                    };
                                }

                                @Override
                                public Class type() {
                                    return morphField.converter().type();
                                }

                                @Override
                                public boolean morph() {
                                    return morphField.converter().morph();
                                }

                                @Override
                                public Class<? extends Annotation> annotationType() {
                                    return morphField.converter().annotationType();
                                }
                            };
                        }

                        @Override
                        public boolean isPrimitiveBoolean() {
                            return morphField.isPrimitiveBoolean();
                        }

                        @Override
                        public boolean list() {
                            return morphField.list();
                        }

                        @Override
                        public Class<? extends Annotation> annotationType() {
                            return morphField.annotationType();
                        }

                    };
                    morphFields[i++] = (morphField1);
                }
                return morphFields;
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return MorphMethod.class;
            }

        };
        return newMorphMethod;
    }

    private String getTarget(Names names) {
        return names.target().isEmpty() ? names.value() : names.target();
    }

}
