package com.grandtech.mapframe.core.util;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.grandtech.mapframe.core.rules.Rules;
import com.mapbox.mapboxsdk.style.layers.PropertyValue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName Expression
 * @Description TODO 过滤表达式帮助类（对mapbox自带的Expression的补充）
 * @Author: fs
 * @Date: 2021/6/3 9:24
 * @Version 2.0
 */
public class Expression  implements Rules {
    @Nullable
    private final String operator;
    @Nullable
    private final Expression[] arguments;

    /**
     * Creates an empty expression for expression literals
     */
    Expression() {
        operator = null;
        arguments = null;
    }

    /**
     * Creates an expression from its operator and varargs expressions.
     *
     * @param operator  the expression operator
     * @param arguments expressions input
     */
    public Expression(@NonNull String operator, @Nullable Expression... arguments) {
        this.operator = operator;
        this.arguments = arguments;
    }


    /**
     * 根据数组的第二个值去替换对应的过滤
     * @param rawExpression 原过滤数组
     * @param item 要替换的值
     * @return
     */
    public static com.mapbox.mapboxsdk.style.expressions.Expression replace
    (@NonNull com.mapbox.mapboxsdk.style.expressions.Expression rawExpression,
     com.mapbox.mapboxsdk.style.expressions.Expression item) {
        try {
            Log.i("iiiExpression", rawExpression.toString());// true报错
            Gson gson = GsonFactory.getFactory().getComGson();
            JsonArray array = gson.fromJson(rawExpression.toString(), JsonArray.class);
            JsonArray elements = gson.fromJson(item.toString(), JsonArray.class);
            //如果没有过此过滤就直接加上不需要递归了
            if(!array.toString().contains(elements.get(1).getAsString())){
                com.mapbox.mapboxsdk.style.expressions.Expression expression = com.mapbox.mapboxsdk.style.expressions.Expression.all(rawExpression, item);
                JsonArray jsonArray = new JsonParser().parse(expression.toString()).getAsJsonArray();
                return com.mapbox.mapboxsdk.style.expressions.Expression.Converter.convert(jsonArray);
            }
            Expression expression = convert(array, elements);
            JsonArray jsonArray = new JsonParser().parse(expression.toString()).getAsJsonArray();
            return com.mapbox.mapboxsdk.style.expressions.Expression.Converter.convert(jsonArray);
        } catch (Exception e) {
            // 异常捕获，解决表达式转换异常
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Returns a string representation of the object that matches the definition set in the style specification.
     * <p>
     * If this expression contains a coma (,) delimited literal, like 'rgba(r, g, b, a)`,
     * it will be enclosed with double quotes (").
     * </p>
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[\"").append(operator).append("\"");
        if (arguments != null) {
            for (Object argument : arguments) {
                builder.append(", ");
                builder.append(argument.toString());
            }
        }
        builder.append("]");
        return builder.toString();
    }
    private static Expression convert(@NonNull JsonArray jsonArray, JsonArray item) {
        int size = jsonArray.size();
        if (size == 0) {
            throw new IllegalArgumentException("Can't convert empty jsonArray expressions");
        }
        String operator = jsonArray.get(0).getAsString();
        String key = null;
        if (size >= 1) {
            JsonElement element = jsonArray.get(1);
            if (element instanceof JsonPrimitive) {
                key = element.getAsString();
            }
        }

        String editKey = item.get(1).getAsString();

        if (editKey.equals(key)) {
            operator = item.get(0).getAsString();
            jsonArray = item;
        }

        final List<Expression> arguments = new ArrayList<>();
        JsonElement jsonElement;
        for (int i = 1; i < jsonArray.size(); i++) {
            jsonElement = jsonArray.get(i);
            if (operator.equals("literal") && jsonElement instanceof JsonArray) {
                JsonArray nestedArray = (JsonArray) jsonElement;
                Object[] array = new Object[nestedArray.size()];
                for (int j = 0; j < nestedArray.size(); j++) {
                    JsonElement element = nestedArray.get(j);
                    if (element instanceof JsonPrimitive) {
                        array[j] = convertToValue((JsonPrimitive) element);
                    } else {
                        throw new IllegalArgumentException("Nested literal arrays are not supported.");
                    }
                }
                arguments.add(new ExpressionLiteralArray(array));
            } else {
                arguments.add(convert(jsonElement,item));
            }
        }
        Expression[] array = arguments.toArray(new Expression[arguments.size()]);
        return new Expression(operator, array);
    }
    /**
     * Converts a JsonElement to an expression
     *
     * @param jsonElement the json element to convert
     * @return the expression
     */
    public static Expression convert(@NonNull JsonElement jsonElement,JsonArray item) {
        if (jsonElement instanceof JsonArray) {
            return convert((JsonArray) jsonElement,item);
        } else if (jsonElement instanceof JsonPrimitive) {
            return convert((JsonPrimitive) jsonElement);
        } else if (jsonElement instanceof JsonNull) {
            return new ExpressionLiteral("");
        } else if (jsonElement instanceof JsonObject) {
            Map<String, Expression> map = new HashMap<>();
            for (String key : ((JsonObject) jsonElement).keySet()) {
                map.put(key, convert(((JsonObject) jsonElement).get(key)));
            }
            return new ExpressionMap(map);
        } else {
            throw new RuntimeException("Unsupported expression conversion for " + jsonElement.getClass());
        }
    }
    public static Expression convert(@NonNull JsonElement jsonElement) {
        if (jsonElement instanceof JsonArray) {
            return convert((JsonArray) jsonElement);
        } else if (jsonElement instanceof JsonPrimitive) {
            return convert((JsonPrimitive) jsonElement);
        } else if (jsonElement instanceof JsonNull) {
            return new ExpressionLiteral("");
        } else if (jsonElement instanceof JsonObject) {
            Map<String, Expression> map = new HashMap<>();
            for (String key : ((JsonObject) jsonElement).keySet()) {
                map.put(key, convert(((JsonObject) jsonElement).get(key)));
            }
            return new ExpressionMap(map);
        } else {
            throw new RuntimeException("Unsupported expression conversion for " + jsonElement.getClass());
        }
    }
    /**
     * Converts a JsonPrimitive to an expression literal
     *
     * @param jsonPrimitive the json primitive to convert
     * @return the expression literal
     */
    private static Expression convert(@NonNull JsonPrimitive jsonPrimitive) {
        return new ExpressionLiteral(convertToValue(jsonPrimitive));
    }
    /**
     * Converts a JsonPrimitive to value
     *
     * @param jsonPrimitive the json primitive to convert
     * @return the value
     */
    private static Object convertToValue(@NonNull JsonPrimitive jsonPrimitive) {
        if (jsonPrimitive.isBoolean()) {
            return jsonPrimitive.getAsBoolean();
        } else if (jsonPrimitive.isNumber()) {
            return jsonPrimitive.getAsFloat();
        } else if (jsonPrimitive.isString()) {
            return jsonPrimitive.getAsString();
        } else {
            throw new RuntimeException("Unsupported literal expression conversion for " + jsonPrimitive.getClass());
        }
    }

    /**
     * ExpressionLiteral wraps an object to be used as a literal in an expression.
     * <p>
     * ExpressionLiteral is created with {@link # literal(Number)}, {@link #(boolean)},
     * {@link # literal(String)} and {@link # literal(Object)}.
     * </p>
     */
    public static class ExpressionLiteral extends Expression implements ValueExpression {

        protected Object literal;

        /**
         * Create an expression literal.
         *
         * @param object the object to be treated as literal
         */
        public ExpressionLiteral(@NonNull Object object) {
            if (object instanceof String) {
                object = unwrapStringLiteral((String) object);
            } else if (object instanceof Number) {
                object = ((Number) object).floatValue();
            }
            this.literal = object;
        }

        /**
         * Get the literal object.
         *
         * @return the literal object
         */
        @Override
        public Object toValue() {
            if (literal instanceof PropertyValue) {
                throw new IllegalArgumentException(
                        "PropertyValue are not allowed as an expression literal, use value instead.");
            } else if (literal instanceof ExpressionLiteral) {
                return ((ExpressionLiteral) literal).toValue();
            }
            return literal;
        }

        @NonNull
        @Override
        public Object[] toArray() {
            return new Object[]{"literal", literal};
        }

        /**
         * Returns a string representation of the expression literal.
         *
         * @return a string representation of the object.
         */
        @Override
        public String toString() {
            String string;
            if (literal instanceof String) {
                string = "\"" + literal + "\"";
            } else {
                string = literal.toString();
            }
            return string;
        }

        /**
         * Indicates whether some other object is "equal to" this one.
         *
         * @param o the other object
         * @return true if equal, false if not
         */
        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }

            ExpressionLiteral that = (ExpressionLiteral) o;

            return literal != null ? literal.equals(that.literal) : that.literal == null;
        }

        /**
         * Returns a hash code value for the expression literal.
         *
         * @return a hash code value for this expression literal
         */
        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + (literal != null ? literal.hashCode() : 0);
            return result;
        }

        @NonNull
        private static String unwrapStringLiteral(String value) {
            if (value.length() > 1 &&
                    value.charAt(0) == '\"' && value.charAt(value.length() - 1) == '\"') {
                return value.substring(1, value.length() - 1);
            } else {
                return value;
            }
        }
    }
    /**
     * Interface used to describe expressions that hold a Java value.
     */
    private interface ValueExpression {
        Object toValue();
    }

    /**
     * Converts the expression to Object array representation.
     * <p>
     * The output will later be converted to a JSON Object array.
     * </p>
     *
     * @return the converted object array expression
     */
    @NonNull
    public Object[] toArray() {
        List<Object> array = new ArrayList<>();
        array.add(operator);
        if (arguments != null) {
            for (Expression argument : arguments) {
                if (argument instanceof ValueExpression) {
                    array.add(((ValueExpression) argument).toValue());
                } else {
                    array.add(argument.toArray());
                }
            }
        }
        return array.toArray();
    }

    /**
     * Wraps an expression value stored in a Map.
     */
    private static class ExpressionMap extends Expression implements ValueExpression {
        private Map<String, Expression> map;

        ExpressionMap(Map<String, Expression> map) {
            this.map = map;
        }

        @NonNull
        @Override
        public Object toValue() {
            Map<String, Object> unwrappedMap = new HashMap<>();
            for (String key : map.keySet()) {
                Expression expression = map.get(key);
                if (expression instanceof ValueExpression) {
                    unwrappedMap.put(key, ((ValueExpression) expression).toValue());
                } else {
                    unwrappedMap.put(key, expression.toArray());
                }
            }

            return unwrappedMap;
        }

        @NonNull
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("{");
            for (String key : map.keySet()) {
                builder.append("\"").append(key).append("\": ");
                builder.append(map.get(key));
                builder.append(", ");
            }

            if (map.size() > 0) {
                builder.delete(builder.length() - 2, builder.length());
            }

            builder.append("}");
            return builder.toString();
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }
            ExpressionMap that = (ExpressionMap) o;
            return map.equals(that.map);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + (map == null ? 0 : map.hashCode());
            return result;
        }
    }
    /**
     * Expression to wrap Object[] as a literal
     */
    private static class ExpressionLiteralArray extends ExpressionLiteral {

        /**
         * Create an expression literal.
         *
         * @param object the object to be treated as literal
         */
        ExpressionLiteralArray(@NonNull Object[] object) {
            super(object);
        }

        /**
         * Convert the expression array to a string representation.
         *
         * @return the string representation of the expression array
         */
        @NonNull
        @Override
        public String toString() {
            Object[] array = (Object[]) literal;
            StringBuilder builder = new StringBuilder("[");
            for (int i = 0; i < array.length; i++) {
                Object argument = array[i];
                if (argument instanceof String) {
                    builder.append("\"").append(argument).append("\"");
                } else {
                    builder.append(argument);
                }

                if (i != array.length - 1) {
                    builder.append(", ");
                }
            }
            builder.append("]");
            return builder.toString();
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            ExpressionLiteralArray that = (ExpressionLiteralArray) o;

            return Arrays.equals((Object[]) this.literal, (Object[]) that.literal);
        }
    }
}
