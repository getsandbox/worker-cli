package com.sandbox.runtime.config;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.sandbox.runtime.HttpServer;
import com.sandbox.runtime.converters.HttpServletConverter;
import com.sandbox.runtime.js.models.Console;
import com.sandbox.runtime.js.models.RuntimeVersion;
import com.sandbox.runtime.js.serializers.ScriptObjectMirrorSerializer;
import com.sandbox.runtime.js.serializers.ScriptObjectSerializer;
import com.sandbox.runtime.js.serializers.UndefinedSerializer;
import com.sandbox.runtime.js.services.JSEngineService;
import com.sandbox.runtime.js.services.RuntimeService;
import com.sandbox.runtime.js.services.ServiceManager;
import com.sandbox.runtime.js.utils.NashornRuntimeUtils;
import com.sandbox.runtime.js.utils.NashornUtils;
import com.sandbox.runtime.js.utils.NashornValidationUtils;
import com.sandbox.runtime.models.Cache;
import com.sandbox.runtime.models.SandboxScriptEngine;
import com.sandbox.runtime.services.InMemoryCache;
import com.sandbox.runtime.services.LiquidRenderer;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;

import javax.script.ScriptEngine;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathFactory;


public abstract class Context {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private Environment environment;

    private static Logger logger = LoggerFactory.getLogger(Context.class);

    static Config config = null;

    static Throwable unwrapException(Throwable e){
        if(e.getCause() != null) return unwrapException(e.getCause());
        return e;
    }

    protected void start(){
        HttpServer httpServer = applicationContext.getBean(HttpServer.class);
        httpServer.start();
    }

    @Bean
    public ObjectMapper objectMapper(){
        return getObjectMapper();
    }

    public static ObjectMapper getObjectMapper(){
        ObjectMapper mapper = new ObjectMapper();

        SimpleModule nashornModule = new SimpleModule("nashornModule", new Version(1, 0, 0, null, null, null));
        nashornModule.addSerializer(new ScriptObjectSerializer());
        nashornModule.addSerializer(new ScriptObjectMirrorSerializer());
        nashornModule.addSerializer(new UndefinedSerializer());
        mapper.registerModule(nashornModule);

        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return mapper;
    }

    @Bean
    @Scope("prototype")
    @Lazy
    public Console instanceConsole() { return new Console(); }

    @Bean(name = "nashornUtils")
    @Scope("prototype")
    @Lazy
    public NashornRuntimeUtils nashornUtils(String sandboxId) { return new NashornRuntimeUtils(sandboxId); }

    @Bean(name = "nashornValidationUtils")
    @Scope("prototype")
    @Lazy
    public NashornUtils nashornValidationUtils() { return new NashornValidationUtils(); }

    @Bean
    @Lazy
    public ScriptEngine scriptEngine() {
        NashornScriptEngineFactory engineFactory = applicationContext.getBean(NashornScriptEngineFactory.class);
        ScriptEngine engine = createScriptEngine(engineFactory);
        return engine;
    }

    public static ScriptEngine createScriptEngine(NashornScriptEngineFactory factory){
        ScriptEngine engine = factory.getScriptEngine(new String[]{"--no-java"});
        return engine;
    }

    @Bean(name = "droneService")
    @Scope("prototype")
    @Lazy
    public RuntimeService runtimeService(SandboxScriptEngine engine, String fullSandboxId, String sandboxId) {
        NashornUtils nashornUtils = (NashornUtils) applicationContext.getBean("nashornUtils", sandboxId);
        return new RuntimeService(engine, nashornUtils, fullSandboxId, sandboxId);
    }

    @Bean
    public Config config(){
        return config;
    }

    @Bean
    public JSEngineService jsEngineService(){
        Config config = applicationContext.getBean(Config.class);
        return new JSEngineService(config.getRuntimeVersion());
    }

    @Bean
    @Scope("prototype")
    public JSEngineService jsEngineService(RuntimeVersion runtimeVersion){
        return new JSEngineService(runtimeVersion);
    }

    @Bean
    public ServiceManager serviceManager(){
        Config config = applicationContext.getBean(Config.class);
        if(config.isDisableRefresh()){
            return new ServiceManager(-1);
        }else{
            return new ServiceManager(250);
        }
    }

    @Bean
    @Lazy
    public Cache getCache(){
        return new InMemoryCache();
    }

    @Bean
    public HttpServletConverter httpServletConverter(){
        return new HttpServletConverter();
    }

    @Bean
    public NashornScriptEngineFactory nashornScriptEngineFactory(){
        return new NashornScriptEngineFactory();
    }

    //keep docuemnt factories around in thread local context
    private static DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    private static ThreadLocal<DocumentBuilder> documentBuilderThreadLocal = new ThreadLocal<>();

    @Bean
    public static DocumentBuilder xmlDocumentBuilder() {
        documentBuilderFactory.setValidating(false);
        DocumentBuilder db = null;
        try {
            db = documentBuilderThreadLocal.get();
            if(db == null){
                db = documentBuilderFactory.newDocumentBuilder();
                documentBuilderThreadLocal.set(db);
            }else{
                db.reset();
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            db = null;
        }
        return db;
    }

    //keep xpathfactories around in thread local context
    private static ThreadLocal<XPathFactory> xPathBuilderThreadLocal = new ThreadLocal<>();

    @Bean
    public static XPathFactory xPathFactory() {
        XPathFactory xPathFactory = xPathBuilderThreadLocal.get();
        if(xPathFactory == null){
            xPathFactory = XPathFactory.newInstance();
            xPathBuilderThreadLocal.set(xPathFactory);
        }
        return xPathFactory;
    }

    @Bean
    public LiquidRenderer liquidRenderer(){
        return new LiquidRenderer();
    }
}
