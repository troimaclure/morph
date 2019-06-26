package com.mycompany.mapper.processors;

import com.google.auto.service.AutoService;
import com.mycompany.mapper.morph.MorphField;
import com.mycompany.mapper.morph.MorphMethod;
import com.mycompany.mapper.morph.MorphNested;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
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
            for (MorphField field : morphMethod.fields()) {
                out.println("." + field.target() + "(p.get" + toCapitalize(field.source()) + "())");
            }
            //.dto(PersonDTONested.builder().scoreDto(p.getNested().getScore()).build())

            for (MorphNested nested : morphMethod.nesteds()) {
                out.println("." + nested.target() + "(" + nested.targetType().getSimpleName() + ".builder()");
                for (MorphField field : nested.fields()) {
                    out.println("." + field.target() + "(" + "p.get" + toCapitalize(nested.source()) + "().get" + toCapitalize(field.source()) + "())");
                }
                out.print(".build())");
            }
            out.print(".build();");
            out.println();
            out.print("}");
        }
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
            }
            for (MorphField nested : morphMethod.fields()) {
                addToImports(nested.sourceType().getCanonicalName());
                addToImports(nested.targetType().getCanonicalName());
            }
        }

        for (String aImport : imports) {
            out.print("import " + aImport + ";");
        }
        out.print("import " + classTarget.getCanonicalName() + ";");
    }

    private void addToImports(String classImport) {
        if (!imports.contains(classImport))
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
