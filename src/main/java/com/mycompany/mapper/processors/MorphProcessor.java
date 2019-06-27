package com.mycompany.mapper.processors;

import com.google.auto.service.AutoService;
import com.mycompany.mapper.utils.JavaLang;
import com.mycompany.mapper.morph.MorphField;
import com.mycompany.mapper.morph.MorphMethod;
import com.mycompany.mapper.morph.MorphNested;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.JavaFileObject;

/**
 *
 * @author ajosse
 */
@SupportedAnnotationTypes("com.mycompany.mapper.morph.Morph")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class MorphProcessor extends AbstractProcessor {

    public ArrayList<String> imports = new ArrayList<>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Elements elementUtils = this.processingEnv.getElementUtils();

        for (TypeElement annotation : annotations) {
            Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);
            for (Element annotatedElement : annotatedElements) {
                PackageElement packageOf = elementUtils.getPackageOf(annotatedElement);
                System.out.println(packageOf.getQualifiedName());
                try {
                    Class<?> forName = Class.forName(packageOf.getQualifiedName() + "." + annotatedElement.getSimpleName());

                    writeFile(packageOf.getQualifiedName().toString(), forName);

                } catch (ClassNotFoundException | IOException ex) {
                    Logger.getLogger(MorphProcessor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        return true;
    }

    private void writeFile(String packageName, Class<?> classTarget) throws IOException {
        String className = classTarget.getSimpleName() + "Impl";
        JavaFileObject builderFile = processingEnv.getFiler()
                .createSourceFile(packageName + "." + className);

        try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {

            writePackage(packageName, out);
            WriteImports(classTarget, out);
            WriteClassAndConstructor(out, className, classTarget);
            writeMethods(classTarget, out);
            writeCloseClass(out);

        } catch (Exception e) {

        }
    }

    private void writeMethods(Class<?> classTarget, final PrintWriter out) throws SecurityException {
        for (Method method : classTarget.getMethods()) {
            MorphMethod morphMethod = method.getAnnotation(MorphMethod.class);

            if (morphMethod == null) continue;
            Class<?> returnType = method.getReturnType();
            Class<?> paramType = method.getParameterTypes()[0];
            out.println("@Override");
            out.print(" public " + returnType.getSimpleName() + " morph(" + paramType.getSimpleName() + " p ) {");
            out.println();
            out.print("return " + returnType.getSimpleName() + ".builder()");

            ArrayList<Field> fields = gatherFieldsMirror(returnType, morphMethod.fields(), paramType);
            writeMethodForMorph(morphMethod.fields(), out, "p");
            writeMethodForField(fields, out, "p");
            for (MorphNested nested : morphMethod.nesteds()) {

                String prefix = "p.get" + toCapitalize(nested.source()) + "()";
                out.println("." + nested.target() + "(" + nested.targetType().getSimpleName() + ".builder()");
                ArrayList<Field> gatherFieldsMirror = gatherFieldsMirror(nested.targetType(), nested.fields(), nested.sourceType());
                writeMethodForField(gatherFieldsMirror, out, prefix);
                writeMethodForMorph(nested.fields(), out, prefix);
                out.print(".build())");
            }
            out.print(".build();");
            out.println();
            out.print("}");
        }
    }

    private void writeMethodForField(ArrayList<Field> fields, final PrintWriter out, String prefix) {
        //all fields not delcared but same with target

        for (Field field : fields) {

            out.println("." + field.getName() + "(" + prefix + ".get" + toCapitalize(field.getName()) + "())");
        }
    }

    private void writeMethodForMorph(MorphField[] fields, final PrintWriter out, String prefix) {
        //all fields declared in annotations
        for (MorphField field : fields) {
            String converter = "";
            String converterEnd = "";
            if (!field.converterType().equals(JavaLang.class)) {
                converter = field.converterType().getSimpleName() + "." + field.converterMethod() + "(";
                converterEnd = ")";
            }
            out.println("." + field.target() + "(" + converter + prefix + ".get" + toCapitalize(field.source()) + "())" + converterEnd);
        }
    }

    private ArrayList<Field> gatherFieldsMirror(Class<?> returnType, MorphField[] fieldsMorph, Class<?> paramType) throws SecurityException {
        ArrayList<Field> fields = new ArrayList<>();
        for (Field declaredField : returnType.getDeclaredFields()) {
            try {
                if (!Arrays.asList(fieldsMorph).stream().anyMatch(e -> e.source().equals(declaredField.getName())))
                    fields.add(paramType.getDeclaredField(declaredField.getName()));
            } catch (NoSuchFieldException ex) {
            }
        }
        return fields;
    }

    private void writeCloseClass(final PrintWriter out) {
        out.println();
        out.print("}");
    }

    private void WriteClassAndConstructor(final PrintWriter out, String className, Class<?> classTarget) {
        out.print("public class " + className + " implements  " + classTarget.getSimpleName() + "{");
        out.println();
        out.print("public " + className + "  (){");
        out.println();
        out.print("}");
    }

    private void WriteImports(Class<?> classTarget, final PrintWriter out) throws SecurityException {
        for (Method method : classTarget.getMethods()) {
            MorphMethod morphMethod = method.getAnnotation(MorphMethod.class);
            if (morphMethod == null) continue;

            addToImports(method.getReturnType().getCanonicalName());
            addToImports(method.getParameterTypes()[0].getCanonicalName());

            for (MorphNested nested : morphMethod.nesteds()) {
                addToImports(nested.sourceType().getCanonicalName());
                addToImports(nested.targetType().getCanonicalName());
                for (MorphField field : nested.fields()) {
                    addToImports(field.converterType().getCanonicalName());
                }
            }
            for (MorphField nested : morphMethod.fields()) {
                addToImports(nested.sourceType().getCanonicalName());
                addToImports(nested.targetType().getCanonicalName());
                addToImports(nested.converterType().getCanonicalName());
            }
        }

        for (String aImport : imports) {
            out.print("import " + aImport + ";");
        }
        out.print("import " + classTarget.getCanonicalName() + ";");
    }

    private void addToImports(String classImport) {
        if (!imports.contains(classImport) && !classImport.equals(JavaLang.class.getCanonicalName()))
            imports.add(classImport);
    }

    private void writePackage(String packageName, final PrintWriter out) {
        if (packageName != null) {
            out.print("package " + packageName + ";");
            out.println();
        }
    }

    private String toCapitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
