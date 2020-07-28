package com.sandbox.worker.core.js;

import com.sandbox.worker.core.js.models.WorkerScriptContext;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

public class JSContextHelper {

    public static Value eval(Context context, String script) {
        Value result;
        synchronized (context) {
            context.resetLimits();
            context.enter();
            try {
                result = context.eval("js", script);
            } catch (PolyglotException e) {
                if (e.isCancelled()) {
                    ContextFactory.removeCancelledContextImmediately(context);
                }
                throw e;

            } finally {
                context.leave();
            }
        }
        return result;
    }

    public static Value eval(Context context, Source source) {
        Value result;
        synchronized (context) {
            context.resetLimits();
            context.enter();
            try {
                result = context.eval(source);
            } catch (PolyglotException e) {
                if (e.isCancelled()) {
                    ContextFactory.removeCancelledContextImmediately(context);
                }
                throw e;

            } finally {
                context.leave();
            }
        }
        return result;
    }

    public static Value eval(WorkerScriptContext context, String script) {
        return eval(context.getExecutionContext(), script);
    }

    public static Value eval(WorkerScriptContext context, Source source) {
        return eval(context.getExecutionContext(), source);
    }

    public static Value execute(Context context, String script, Object... arguments) {
        return execute(context, eval(context, script), arguments);
    }

    public static Value execute(Context context, Value function, Object... arguments) {
        Value result;
        synchronized (context) {
            context.resetLimits();
            context.enter();
            try {
                result = function.execute(arguments);
            } catch (PolyglotException e) {
                if (e.isCancelled()) {
                    ContextFactory.removeCancelledContextImmediately(context);
                }
                throw e;

            } finally {
                context.leave();
            }
        }
        return result;
    }

    public static Value execute(WorkerScriptContext context, String script, Object... arguments) {
        return execute(context.getExecutionContext(), script, arguments);
    }

    public static Value execute(WorkerScriptContext context, Value function, Object... arguments) {
        return execute(context.getExecutionContext(), function, arguments);
    }

    public static Value get(Context context, String key) {
        Value result;
        synchronized (context) {
            context.enter();
            try {
                result = context.getBindings("js").getMember(key);
            } finally {
                context.leave();
            }
        }
        return result;
    }

    public static Value get(WorkerScriptContext context, String key) {
        return get(context.getExecutionContext(), key);
    }

    public static void put(Context context, String key, Object value) {
        synchronized (context) {
            context.enter();
            try {
                context.getBindings("js").putMember(key, value);
            } finally {
                context.leave();
            }
        }
    }

    public static void put(WorkerScriptContext context, String key, Object value) {
        put(context.getExecutionContext(), key, value);
    }

    public static void remove(Context context, String key) {
        synchronized (context) {
            context.enter();
            try {
                context.getBindings("js").removeMember(key);
            } finally {
                context.leave();
            }
        }
    }

}
