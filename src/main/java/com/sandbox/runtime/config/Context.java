package com.sandbox.runtime.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.sandbox.runtime.converters.HttpServletConverter;
import com.sandbox.runtime.js.models.Console;
import com.sandbox.runtime.js.services.JSEngineQueue;
import com.sandbox.runtime.js.services.RuntimeService;
import com.sandbox.runtime.js.utils.INashornUtils;
import com.sandbox.runtime.js.utils.NashornRuntimeUtils;
import com.sandbox.runtime.js.utils.NashornValidationUtils;
import com.sandbox.runtime.models.Cache;
import com.sandbox.runtime.models.SandboxScriptEngine;
import com.sandbox.runtime.services.CommandLineProcessor;
import com.sandbox.runtime.services.InMemoryCache;
import com.sandbox.runtime.services.LiquidRenderer;
import com.pivotal.cf.mobile.ats.json.ScriptObjectMirrorSerializer;
import com.pivotal.cf.mobile.ats.json.ScriptObjectSerializer;
import com.pivotal.cf.mobile.ats.json.UndefinedSerializer;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.core.env.SimpleCommandLinePropertySource;

import javax.script.ScriptEngine;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathFactory;

@Configuration
@ComponentScan(basePackages = {"com.sandbox.runtime"},
	excludeFilters = { @ComponentScan.Filter( Configuration.class ) }
)
public class Context {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private Environment environment;

    private static Logger logger = LoggerFactory.getLogger(Context.class);

    public static void main(String[] args) {
        try {
            AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
            context.register(Context.class);

            context.getEnvironment().getPropertySources().addLast(new SimpleCommandLinePropertySource(args));

            context.refresh();
            context.start();

            //process command line args and kick off
            CommandLineProcessor command = context.getBean(CommandLineProcessor.class);
            command.process();

        }catch(Throwable e){
            logger.error("Error starting runtime - " + unwrapException(e).getMessage());
            System.exit(1);
        }
    }

    private static Throwable unwrapException(Throwable e){
        if(e.getCause() != null) return unwrapException(e.getCause());
        return e;
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
        mapper.disable(SerializationFeature.WRITE_NULL_MAP_VALUES);
        mapper.disable(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
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
    public INashornUtils nashornValidationUtils() { return new NashornValidationUtils(); }

    @Bean
    @Lazy
    public JSEngineQueue engineQueue(){
        JSEngineQueue JSEngineQueue = new JSEngineQueue(50, applicationContext);
        JSEngineQueue.start();
        return JSEngineQueue;
    }

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

    @Bean
    @Scope("prototype")
    @Lazy
    public RuntimeService droneService() {
        SandboxScriptEngine engine = applicationContext.getBean(JSEngineQueue.class).get();
        return new RuntimeService(engine);
    }

    @Bean
    public CommandLineProcessor getCommandLineProcessor() { return new CommandLineProcessor(); }

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
