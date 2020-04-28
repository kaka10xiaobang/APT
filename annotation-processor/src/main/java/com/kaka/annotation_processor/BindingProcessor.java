package com.kaka.annotation_processor;

import com.google.auto.service.AutoService;
import com.kaka.annotation.TestBindView;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class BindingProcessor extends AbstractProcessor {

    private Filer mFiler;//文件类
    private Messager mMessager;//打印错误信息
    private static final Map<TypeElement, List<ViewInfo>> bindViews = new HashMap<>();//绑定的view集合


    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mFiler = processingEnvironment.getFiler();//初始化文件对象
        mMessager = processingEnvironment.getMessager();//初始化信息对象

    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(TestBindView.class);//获取TestBindView注解的所有元素
        for (Element element : elements) {//遍历元素
            VariableElement variableElement = (VariableElement) element;//因为注解的作用域是成员变量，所以这里可以直接强转成 VariableElement
            Set<Modifier> modifiers = variableElement.getModifiers();//权限修饰符
            if (modifiers.contains(Modifier.PRIVATE) || modifiers.contains(Modifier.PROTECTED)) {//类型检查
                mMessager.printMessage(Diagnostic.Kind.ERROR, "成员变量的类型不能是PRIVATE或者PROTECTED");
                return false;
            }
            TypeElement typeElement = (TypeElement) variableElement.getEnclosingElement();//获得外部元素对象
            sortToAct(typeElement, variableElement);//以类元素进行分类
        }

        writeToFile();

        return false;
    }


    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> stringSet = new HashSet<>();
        stringSet.add(TestBindView.class.getCanonicalName());//返回需要注解的类名
        return stringSet;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    /**
     * 把view信息跟activity关联在一起
     */
    private void sortToAct(TypeElement typeElement, VariableElement variableElement) {

        List<ViewInfo> viewInfos;
        if (bindViews.get(typeElement) != null) {
            viewInfos = bindViews.get(typeElement);
        } else {
            viewInfos = new ArrayList<>();
        }
        TestBindView annotation = variableElement.getAnnotation(TestBindView.class);
        int viewId = annotation.value();
        String viewName = variableElement.getSimpleName().toString();
        ViewInfo viewInfo = new ViewInfo(viewId, viewName);
        viewInfos.add(viewInfo);
        bindViews.put(typeElement, viewInfos);
    }

    /**
     * 生成文件
     */
    private void writeToFile() {
        Set<TypeElement> typeElements = bindViews.keySet();
        String paramName = "target";
        for (TypeElement typeElement : typeElements) {
            ClassName className = ClassName.get(typeElement);//获取参数类型
            PackageElement packageElement = (PackageElement) typeElement.getEnclosingElement();//获得外部对象
            String packageName = packageElement.getQualifiedName().toString();//获得包名
            List<ViewInfo> viewInfos = bindViews.get(typeElement);
            CodeBlock.Builder builder = CodeBlock.builder();//代码块对象
            for (ViewInfo viewInfo : viewInfos) {
                //生成代码
                builder.add(paramName + "." + viewInfo.getViewName() + " = " + paramName + ".findViewById(" + viewInfo.getViewId() + ");\n");

            }

            FieldSpec fieldSpec = FieldSpec.builder(String.class,"name",Modifier.PRIVATE).build();//成员变量

            MethodSpec methodSpec = MethodSpec.constructorBuilder()//生成的方法对象
                    .addModifiers(Modifier.PUBLIC)//方法的修饰符
                    .addParameter(className, paramName)//方法中的参数，第一个是参数类型，第二个是参数名
                    .addCode(builder.build())//方法体重的代码
                    .build();

            TypeSpec typeSpec = TypeSpec.classBuilder(typeElement.getSimpleName().toString() + TestBindView.SUFFIX)//类对象，参数：类名
                    .addMethod(methodSpec)//添加方法
                    .addField(fieldSpec)//添加成员变量
                    .build();

            JavaFile javaFile = JavaFile.builder(packageName, typeSpec).build();//javaFile对象，最终用来写入的对象，参数1：包名；参数2：TypeSpec

            try {
                javaFile.writeTo(mFiler);//写入文件
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


    }
}
