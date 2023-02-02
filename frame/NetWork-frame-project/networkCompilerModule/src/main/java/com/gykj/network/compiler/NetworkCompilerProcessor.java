package com.gykj.network.compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import com.google.auto.service.AutoService;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;

/**
 * @author zyp
 * 2021/5/31
 */
@AutoService(Processor.class)
public class NetworkCompilerProcessor extends AbstractProcessor {

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_8;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new HashSet<>();
        set.add(GET.class.getCanonicalName());
        set.add(POST.class.getCanonicalName());
        set.add(PUT.class.getCanonicalName());
        set.add(DELETE.class.getCanonicalName());
        return set;
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> elementsMethod1 = roundEnvironment.getElementsAnnotatedWith(GET.class);
        Set<? extends Element> elementsMethod2 = roundEnvironment.getElementsAnnotatedWith(POST.class);
        Set<? extends Element> elementsMethod3 = roundEnvironment.getElementsAnnotatedWith(PUT.class);
        Set<? extends Element> elementsMethod4 = roundEnvironment.getElementsAnnotatedWith(DELETE.class);

        List<Element> elements = new ArrayList<>();
        elements.addAll(elementsMethod1);
        elements.addAll(elementsMethod2);
        elements.addAll(elementsMethod3);
        elements.addAll(elementsMethod4);
        if(elements.size() > 0){
            //key:所在接口 value:接口中的函数
            Map<Element, List<Element>> apiMap = new HashMap<>();
            for (Element element : elements) {
                Element enclosingElement = element.getEnclosingElement();
                List<Element> methodList = apiMap.get(enclosingElement);
                if (methodList == null) {
                    methodList = new ArrayList<>();
                    apiMap.put(enclosingElement, methodList);
                }
                methodList.add(element);
            }
            ApiProxyInterfaceCreator.generateJavaFile(processingEnv,apiMap);
        }
        return true;
    }
}
