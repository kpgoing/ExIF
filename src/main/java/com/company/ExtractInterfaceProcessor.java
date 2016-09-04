package com.company;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by xubowei on 16/9/4.
 */
@SupportedAnnotationTypes("com.company.ExtractInterface")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class ExtractInterfaceProcessor extends AbstractProcessor {

    private ArrayList<ExecutableElement> interfaceMethods = new ArrayList<>();


    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {


        Messager messager = processingEnv.getMessager();
        for (TypeElement annotation : annotations) {
            for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
                if (element.getKind() == ElementKind.CLASS) {
                    ExtractInterface extractInterface = element.getAnnotation(ExtractInterface.class);
                    List<? extends Element> enclosedElements = element.getEnclosedElements();
                    List<ExecutableElement> executableElements = ElementFilter.methodsIn(enclosedElements);
                    for (ExecutableElement executableElement : executableElements) {
                        if (executableElement.getModifiers().contains(Modifier.PUBLIC) && !executableElement.getModifiers().contains(Modifier.STATIC)) {
                            interfaceMethods.add(executableElement);
                        }
                    }
                    if (interfaceMethods.size() > 0) {
                        try {
                            messager.printMessage(Diagnostic.Kind.NOTE, "begin process " + element + "...");
                            JavaFileObject javaFileObject = processingEnv.getFiler().createSourceFile(extractInterface.value());
                            PrintWriter writer = new PrintWriter(javaFileObject.openWriter());
                            writer.println("package " +
                                    element.getEnclosingElement() + ";");
                            writer.println("public interface " +
                                    extractInterface.value() + " {");
                            for(ExecutableElement m : interfaceMethods) {
                                writer.print("  public ");
                                writer.print(m.getReturnType() + " ");
                                writer.print(m.getSimpleName() + " (");
                                int i = 0;
                                for(VariableElement parm :
                                        m.getParameters()) {
                                    writer.print(parm.asType() + " " +
                                            parm.getSimpleName());
                                    if(++i < m.getParameters().size())
                                        writer.print(", ");
                                }
                                writer.println(");");
                            }
                            writer.println("}");
                            writer.close();
                            messager.printMessage(Diagnostic.Kind.NOTE, extractInterface.value() + " processed successful!" );

                        } catch (IOException e) {
                            messager.printMessage(Diagnostic.Kind.WARNING, extractInterface.value() + " processed fail!" );
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return true;
    }
}
