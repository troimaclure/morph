package com.mycompany.mapper.processors;

import com.google.auto.service.AutoService;
import com.mycompany.mapper.morph.MorphField;
import com.mycompany.mapper.morph.MorphMethod;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
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
import javax.lang.model.element.Modifier;
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
                    for (Method method : forName.getMethods()) {
                        System.out.println(method.getName());
                        MorphMethod morphMethod = method.getAnnotation(MorphMethod.class);

                        if (morphMethod == null) continue;
                        Class<?> returnType = method.getReturnType();
                        Class<?> paramType = method.getParameterTypes()[0];
                        writeFile(returnType, paramType, morphMethod, packageOf.getQualifiedName().toString(), forName);
                    }
//                PackageElement packageElement = (PackageElement) annotatedElement.getEnclosingElement();
//                System.out.println(packageElement.);
//                System.out.println(annotatedElement.getEnclosingElement().getSimpleName());
//                Morph morph = annotatedElement.getAnnotation(Morph.class);
//                if (morph == null) continue;
//                if (morph == null) continue;
//                Class.forName(annotatedElement.getSimpleName()); 
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(MorphProcessor.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(MorphProcessor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        return true;
    }

    private void writeFile(Class<?> returnType, Class<?> paramType, MorphMethod morphMethod, String packageName, Class<?> classTarget) throws IOException {
        String className = classTarget.getSimpleName() + "Impl";
        JavaFileObject builderFile = processingEnv.getFiler()
                .createSourceFile(packageName + "." + className);
//
//        MethodSpec morph = MethodSpec.methodBuilder("morph")
//                .addModifiers(Modifier.PUBLIC)
//                .returns(void.class)
//                .addStatement("System.out.println($S)", "Coucou").build();
//        TypeSpec classe = TypeSpec.classBuilder(className).addModifiers(Modifier.PUBLIC).addMethod(morph).build();
//        
//        JavaFile javaFile = JavaFile.builder(packageName, classe).build();
//        javaFile.writeTo();
//        

        try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {

            if (packageName != null) {
                out.print("package " + packageName + ";");
                out.println();
                out.print("import " + returnType.getCanonicalName() + ";");
                out.print("import " + paramType.getCanonicalName() + ";");
                out.print("import " + classTarget.getCanonicalName() + ";");
            }
            out.print("public class " + className + " implements  " + classTarget.getSimpleName() + "{");
            out.println();
            out.print("public " + className + "  (){");
            out.println();
            out.print("}");

            out.println("@Override");
            out.print(" public " + returnType.getSimpleName() + " morph(" + paramType.getSimpleName() + " p ) {");
            out.println();
            out.print("return " + returnType.getSimpleName() + ".builder()");
            for (MorphField field : morphMethod.fields()) {
                out.println("." + field.target() + "(p.get" + toCapitalize(field.source()) + "())");
            }
            out.print(".build();");
            out.println();
            out.print("}");
            out.println();
            out.print("}");

        } catch (Exception e) {

        }
    }

    private String toCapitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
