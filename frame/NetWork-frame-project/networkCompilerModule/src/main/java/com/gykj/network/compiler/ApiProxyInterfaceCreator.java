package com.gykj.network.compiler;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.tools.JavaFileObject;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;

/**
 * @author zyp
 * 2021/5/31
 */
public class ApiProxyInterfaceCreator {
    private static Map<String, JavaFileObject> javaFileObjectMap = new HashMap<>();
    
    private static void generateMethod4Class(Element element){
        if (element.getKind().isClass()) {
            List<? extends Element> enclosedElements = element.getEnclosedElements();
            for (Element enclosedElement : enclosedElements) {
                generateMethod(enclosedElement);
            }
        }
    }
    
    private static String generateMethod(Element element){
        if (element instanceof Symbol.MethodSymbol) {
            Symbol.MethodSymbol methodSymbol = (Symbol.MethodSymbol) element;
            if ("<init>".equals(methodSymbol.name.toString())) return null;//忽略构造方法
            com.sun.tools.javac.util.List<Symbol.VarSymbol> params = methodSymbol.params;
            com.sun.tools.javac.util.List<Type> argtypes = methodSymbol.type.asMethodType().argtypes;
            if (params.size() != argtypes.size()) {
                return null;
            }
            StringBuilder stringBuilder = new StringBuilder();
            String parameterizedType = getParameterizedType(element);
            stringBuilder.append("      RequestBuilder");
            if(parameterizedType != null){
                stringBuilder.append("<"+parameterizedType+">");
            }
                    stringBuilder.append(" ").append(methodSymbol.name)
                    .append("(");
            for (int i = 0; i < argtypes.size(); i++) {
                stringBuilder.append(argtypes.get(i).toString())
                        .append(" ").append(params.get(i).name.toString());
                if (i != params.size() - 1) {
                    stringBuilder.append(",");
                }
            }
            stringBuilder.append(");");
           // System.out.println("生成函数:" + stringBuilder.toString());
            return stringBuilder.toString();
        }
        return null;
    }
    
    public static void generateJavaFile(ProcessingEnvironment processingEnvironment, Map<Element, List<Element>> apiMap){
       
        for (Element element : apiMap.keySet()) {
            String apiFullName = element.toString();
            String apiSimpleName = element.getSimpleName().toString();
            String packageName = null;
            if(apiFullName.contains(".")){
                packageName = apiFullName.replace("."+apiSimpleName,"").trim();
            }
            String interfaceName = "I"+apiSimpleName+"Proxy";
            JavaFileObject javaFileObject = javaFileObjectMap.get(apiFullName);
            if(javaFileObject == null){
                try {
                    if(packageName != null){
                        javaFileObject = processingEnvironment.getFiler().createSourceFile(packageName +"."+interfaceName);
                    }else {
                        throw new RuntimeException("接口所在类必须有包名!");
                       // javaFileObject = processingEnvironment.getFiler().createSourceFile(interfaceName);
                    }
                   
                    javaFileObjectMap.put(apiFullName,javaFileObject);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(javaFileObject == null) continue;
            String apiCode = generateJavaCode4Interface(packageName, interfaceName,apiSimpleName, apiMap.get(element));
            try {
                Writer writer = javaFileObject.openWriter();
                writer.write(apiCode);
                writer.close();
                System.err.println("生成完成:"+apiFullName);
            } catch (IOException e) {
                e.printStackTrace();
            }
          
        }
    }

    /**
     * 
     * @param packageName 包名
     * @param interfaceName 要生成的接口名
     * @param className 要代理的类名
     * @param methods 其中的方法
     * @return
     */
    private static String generateJavaCode4Interface(String packageName,String interfaceName ,String className,List<Element> methods){
       
        StringBuilder sb = new StringBuilder();
        //设置包名
        if(packageName != null && !"".equals(packageName)){
            sb.append("package ").append(packageName).append(";\n\n");
        }

        //设置import部分
        sb.append("import com.gykj.networkmodule.IBaseApi;\n");
        sb.append("import com.gykj.networkmodule.RealApi;\n");
        sb.append("import com.gykj.networkmodule.RequestBuilder;\n\n");
        //设置注解
        sb.append("@RealApi(").append(packageName).append(".").append(className).append(".class)").append("\n");
        //设置接口名
        sb.append("public interface ").append(interfaceName).append(" extends IBaseApi");
        sb.append("{").append("\n");
        //写入函数
        for (int i = 0; i < methods.size(); i++) {
            Element method = methods.get(i);
            String methodCode = generateMethod(method);
            if(methodCode != null){
                sb.append(genMethodAnnotation(packageName,className,method));
                sb.append(methodCode).append("\n");//写入代码
                if(i != methods.size() -1 ){//不是最后一个方法添加换行
                    sb.append("\n");
                }
            }
        }
        //类结尾
        sb.append("}");
        return sb.toString();
    }

    /**
     * 生成方法注释 方便点击跳转到原声明
     * @return
     */
    private static String genMethodAnnotation(String packageName,String className,Element elementMethod){
        Symbol.MethodSymbol methodSymbol = (Symbol.MethodSymbol) elementMethod;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("      ").append("/**").append("\n");
        stringBuilder.append("      ").append("*")
                .append("{@link ")
                .append(packageName)
                .append(".").append(className)
                .append("#").append(methodSymbol.name)
                .append("}").append("\n");
        stringBuilder.append("      ").append("*/").append("\n");
        return stringBuilder.toString();
    }
    /**
     * 获取方法的泛型参数
     * @param elementMethod e.g. Observable<DataResponse<List<GeoBean>>> getGeos(@Query("name") String name);
     * @return e.g. DataResponse<List<GeoBean>>
     */
    private static String getParameterizedType(Element elementMethod){
        Symbol.MethodSymbol methodSymbol = (Symbol.MethodSymbol) elementMethod;
        Type.MethodType type = (Type.MethodType) methodSymbol.type;
        Type.ClassType restype = (Type.ClassType) type.restype;
        List<Type> typarams_field = restype.typarams_field;
        if(typarams_field != null && !typarams_field.isEmpty()){
            Type parameterizedType = typarams_field.get(0);
            return parameterizedType.toString();
        }else {
            return null;
        }
    }
}
