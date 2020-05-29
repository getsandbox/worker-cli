package com.sandbox.worker.core.js;

import com.sandbox.worker.core.exceptions.ServiceScriptException;
import com.sandbox.worker.core.js.models.RouteDetailsProjection;
import com.sandbox.worker.core.js.models.WorkerScriptContext;
import com.sandbox.worker.core.utils.ErrorUtils;
import com.sandbox.worker.models.DefaultRoutingTable;
import com.sandbox.worker.models.SandboxIdentifier;
import com.sandbox.worker.models.interfaces.Route;
import com.sandbox.worker.models.interfaces.RoutingTable;
import com.sandbox.worker.models.interfaces.SandboxMetadata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenerateRoutingTableExecutor extends AbstractJSExecutor<SandboxIdentifier, RoutingTable> {

    private static final Logger LOG = LoggerFactory.getLogger(GenerateRoutingTableExecutor.class);

    private RouteDetailsProjection routeDetailsProjection;

    public GenerateRoutingTableExecutor(RouteDetailsProjection routeDetailsProjection) {
        this.routeDetailsProjection = routeDetailsProjection;
    }

    @Override
    protected RoutingTable doExecute(SandboxIdentifier sandboxIdentifier, WorkerScriptContext scriptContext) throws ServiceScriptException {
        LOG.debug("{} - Generating routing table", sandboxIdentifier.toString());
        scriptContext.getScriptObject().setDesiredRouteProjection(routeDetailsProjection);

        try {
            bootstrap(scriptContext);
        } catch (Exception e) {
            throw ErrorUtils.getServiceScriptException(e);
        }

        //bootstrapping is done so all define() etc should have been called and our routes should be available
        RoutingTable routingTable = new DefaultRoutingTable();
        routingTable.setRepositoryId(sandboxIdentifier.getFullSandboxId());
        routingTable.setRouteDetails(scriptContext.getScriptObject().getRoutes());

        //enrich with route config
        SandboxMetadata sandboxMetadata = scriptContext.getMetadata();
        if (sandboxMetadata.getRouteConfig() != null) {
            for (Route tableRoute : routingTable.getRouteDetails()){
                if(sandboxMetadata.getRouteConfig().containsKey(tableRoute.getRouteIdentifier())) {
                    tableRoute.setRouteConfig(sandboxMetadata.getRouteConfig().get(tableRoute.getRouteIdentifier()));
                }
            }
        }

        //set routing table into scriptFunctions to re-used where possible
        scriptContext.setRoutingTable(routingTable);
        LOG.debug("{} - Generated routing table", sandboxIdentifier.toString());
        return routingTable;
    }
}
