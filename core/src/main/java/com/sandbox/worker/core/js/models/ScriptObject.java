package com.sandbox.worker.core.js.models;

import com.oracle.truffle.js.runtime.GraalJSException;
import com.oracle.truffle.js.runtime.JSException;
import com.oracle.truffle.object.DynamicObjectBasic;
import com.sandbox.worker.RouteSupport;
import com.sandbox.worker.core.exceptions.ServiceScriptException;
import com.sandbox.worker.models.DefaultHTTPRoute;
import com.sandbox.worker.models.RouteDefinitionSource;
import com.sandbox.worker.models.RuntimeRequest;
import com.sandbox.worker.models.ScriptSource;
import com.sandbox.worker.models.interfaces.Route;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;

@HostAccess.Implementable
public class ScriptObject {

    private static Pattern functionArguments = Pattern.compile("^function\\s*[^\\(]*\\(\\s*([^\\)]*)\\)", 8);
    private static Pattern manyArrowArguments = Pattern.compile("^\\s*\\(([a-zA-Z0-9 ,_]*)\\)\\s*=>", 8);
    private static Pattern functionComments = Pattern.compile("((\\/\\/.*$)|(\\/\\*[\\s\\S]*?\\*\\/))", 40);
    private static String tempDirPrefix = System.getProperty("java.io.tmpdir");

    private HashMap<Route, Value> routes = new HashMap<>();
    private RouteDetailsProjection desiredRouteProjection = RouteDetailsProjection.REQUEST;

    @HostAccess.Export
    public void define(String transport, String defineType, String path, String method, Map<String, Object> properties, Value callbackValue, Value wrappedCallbackValue, Value error) throws ServiceScriptException {
        Map<String, String> propertiesMap = new HashMap<>();
        properties.forEach((key, value) -> {
            if (value instanceof String) propertiesMap.put(key, (String) value);
            return;
        });

        Route routeDetails = new DefaultHTTPRoute(method, path, propertiesMap);
        routeDetails.setTransport(transport);
        if (desiredRouteProjection == RouteDetailsProjection.EDITOR) {
            routeDetails.setDefinitionSource(
                    //set ~reasonable limit for function implementation size, really just a nice-to-have.
                    new RouteDefinitionSource(defineType, getScriptSourceFromError(error, "<sandbox-internal>"), getScriptSourceFromFunction(callbackValue))
            );
        }

        //put the wrapped value rather than the direct function to enable the json schema validation functions
        routes.put(routeDetails, wrappedCallbackValue);
    }

    public List<Route> getRoutes() {
        return new ArrayList<>(routes.keySet());
    }

    public Value getMatchedFunction(RuntimeRequest req) {
        Optional<Route> matchedRoute = routes.keySet().stream().filter(r -> RouteSupport.isMatch(r, req)).findFirst();
        return matchedRoute.isPresent() ? routes.get(matchedRoute.get()) : null;
    }

    public void setDesiredRouteProjection(RouteDetailsProjection desiredRouteProjection) {
        this.desiredRouteProjection = desiredRouteProjection;
    }

    private ScriptSource getScriptSourceFromFunction(Value valueFunction) {
        ScriptSource scriptSource = new ScriptSource();
        org.graalvm.polyglot.SourceSection sourceSection = valueFunction.getSourceLocation();
        String scriptPath = trimTemporaryPathIfRequired(sourceSection.getSource().getPath() == null ? sourceSection.getSource().getName() : sourceSection.getSource().getPath());

        scriptSource.setPath(scriptPath);
        scriptSource.setLineNumber(sourceSection.getStartLine());
        String implementation = sourceSection.getCharacters().toString();

        functionComments.matcher(implementation).replaceAll("");
        Matcher functionMatcher = functionArguments.matcher(implementation);
        Matcher arrowMatcher = manyArrowArguments.matcher(implementation);
        if (functionMatcher.find()) {
            String paramsStr = functionMatcher.group(1);
            if (paramsStr.indexOf(",") == -1) {
                scriptSource.setRequestParameter(paramsStr.trim());
            } else {
                String[] params = paramsStr.split(",");
                scriptSource.setRequestParameter(params[0].trim());
                scriptSource.setResponseParameter(params[1].trim());
            }
        } else if (arrowMatcher.find()) {
            String paramsStr = arrowMatcher.group(1);
            if (paramsStr.indexOf(",") == -1) {
                scriptSource.setRequestParameter(paramsStr.trim());
            } else {
                String[] params = paramsStr.split(",");
                scriptSource.setRequestParameter(params[0].trim());
                scriptSource.setResponseParameter(params[1].trim());
            }
        }

        scriptSource.setImplementation(implementation);
        return scriptSource;
    }

    private ScriptSource getScriptSourceFromError(Value error, String elementIdentifier) {
        ScriptSource scriptSource = new ScriptSource();
        JSException jsException = getInternalObjectFromValue(error, JSException.class);

        if (jsException != null) {
            GraalJSException.JSStackTraceElement[] stack = jsException.getJSStackTrace();
            for (int x = 0; x < stack.length; x++) {
                GraalJSException.JSStackTraceElement element = stack[x];
                GraalJSException.JSStackTraceElement nextElement = (stack.length - 1 >= x + 1) ? stack[x + 1] : null;
                if (element != null && element.getFileName().contains(elementIdentifier)) {
                    scriptSource.setPath(trimTemporaryPathIfRequired(nextElement.getFileName()));
                    scriptSource.setLineNumber(nextElement.getLineNumber());
                    break;
                }
            }
        }

        return scriptSource;
    }

    private <T> T getInternalObjectFromValue(Value value, Class resultType) {
        try {
            Field receivedField = value.getClass().getDeclaredField("receiver");
            receivedField.setAccessible(true);
            DynamicObjectBasic receiver = (DynamicObjectBasic) receivedField.get(value);

            List<String> fields = Arrays.asList("object1", "object2", "object3", "object4");
            for (String field : fields) {
                Field object = receiver.getClass().getDeclaredField(field);
                object.setAccessible(true);
                Object result = object.get(receiver);
                if (resultType.isInstance(result)) {
                    return (T) result;
                }
            }

        } catch (Exception e) {
            //ignore as expect missing sometimes
        }
        return null;
    }

    private String trimTemporaryPathIfRequired(String rawPath){
        if (rawPath.startsWith(tempDirPrefix)) {
            Path scriptPath = new File(rawPath).toPath();
            return scriptPath.subpath(2, scriptPath.getNameCount()).toString();
        } else {
            return rawPath;
        }
    }

}
