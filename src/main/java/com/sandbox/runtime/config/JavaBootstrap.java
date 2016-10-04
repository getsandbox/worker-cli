package com.sandbox.runtime.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"com.sandbox.runtime"},
        excludeFilters = { @ComponentScan.Filter( Configuration.class ) }
)
public class JavaBootstrap extends Context {

    private static Logger logger = LoggerFactory.getLogger(JavaBootstrap.class);

    private Thread startThread;

    public static void start(Config config) {
        //validate config
        config.validate();
        //keep config for context bootstrap
        JavaBootstrap.config = config;

        JavaBootstrap javaBootstrap = new JavaBootstrap();
        javaBootstrap.startInstance(config);
    }

    public void startInstance(Config config){
        //stop first, just incase we are already running
        stopInstance();
        //start new thread to bootstrap context
        startThread = new Thread(()-> {
            try {
                AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(JavaBootstrap.class);
                context.start();
                //start server
                context.getBean(Context.class).start();

            }catch(Throwable e){
                logger.error("Error starting runtime");
                unwrapException(e).printStackTrace();
                System.exit(1);
            }
        });
        startThread.start();
    }

    public void stopInstance(){
        //if we have a start thread already, interrupt it to stop it.
        if(startThread != null){
            logger.info("Stopping instance..");
            startThread.interrupt();
            startThread = null;
        }else{
            logger.debug("Instance not running, ignoring stop.");
        }
    }

}
