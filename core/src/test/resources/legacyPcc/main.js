
    // Get Account details
Sandmox.define('/accounts/:accountNumber', 'get', function(request, response) {
    response.header("Content-Type","application/json")
    var accountNumber = getAccountNumber(request);

    if (accountNumber !== undefined && accountNumber == request.params.accountNumber) {
        var accountDetails = {
            accountNumber : accountNumber
        }
        response.render('accountDetails', accountDetails);
    } else {
        response.json(404, { error: { message: 'Account not found' } })
    }
})

// Create Shipment - Add shipment to account basket
Sandmox.define('/shipments', 'put', function(request, response) {
    response.header("Content-Type","application/json")

    var account = getAccount(request);
    if(account != undefined) {
        var shipments = request.body.shipments;
        var newShipments = createShipments(account, shipments)

        //cant use shipment from inside the function
        response.send(201, {"shipments" : newShipments})
    } else{
        response.send(400,'Unable to create shipment, please check your payload and try again')
    }
})

// Create Bulk Shipment (Order)
Sandmox.define('/shipments', 'post', function(request, response) {
    response.header("Content-Type","application/json")

    var account = getAccount(request);
    if(account != undefined) {
        var shipments = JSON.parse(JSON.stringify(request.body.shipments));

        //create new order
        var order = createOrder(request.body.order_reference, shipments);
        //add new order to orders for account
        account.orders.push(order)
        response.send(201, {"order" : order})
    } else{
        response.send(400,'Unable to create order, please check your payload and try again')
    }
})

// Create Order - Dispatch/Lodge
Sandmox.define('/shipments/dispatch/order', 'post', function(request, response) {
    var account = getAccount(request);
    if(account != undefined) {
        var shipmentsToLodge = request.body.shipments;
        var shipmentIds = _.map(shipmentsToLodge, "shipment_id");
        var shipments = getShipmentsByIds(account,shipmentIds);

        var order = createOrder(request.body.order_reference, shipments);
        //as the shipments have been moved into an order state remove from account.shipments
        console.log('before:' + account.shipments.length)
        _.each(order.shipments, function(shipment) {
            _.each(account.shipments, function(_shipment, index){
                //guard against an undefined shipment
                if(!_shipment || shipment) return
                if(_shipment.shipment_id == shipment.shipment_id){
                    account.shipments.splice(index,1)
                }
            })
        })
        console.log('after:' + account.shipments.length)
        //add new order to orders for account
        account.orders.push(order)
        response.send(201, {"order" : order})
    } else{
        response.send(400,'Unable to create order, please check your payload and try again')
    }
})

// Get Order by ID
Sandmox.define('/locations/:locationId/order/:orderId', 'get', function(request, response) {
    response.header("Content-Type","application/json")

    if(request.params.locationId != undefined && request.params.locationId.length > 0 && request.params.orderId != undefined && request.params.orderId.length > 0) {
        var account = getAccount(request);
        if(account != undefined) {
            var order = getOrderById(account, request.params.orderId)
            if(!_.isEmpty(order)) {
                response.send(200, {"order" : order})
            } else {
                response.send(404, "Not Found ({\"errors\":[{\"message\":\"An order with order id " + request.params.orderId  + " cannot be found. Please check that the identifier is correct and submit the request again.\",\"code\":\"ORDER_NOT_FOUND\",\"id\":44026}]})")
            }
        } else{
            response.send(400,'Unable to find order, please check your payload and try again')
        }
    }
})

// Get Shipment By ID(s)
Sandmox.define('/shipments/location/:locationId', 'get', function(request, response) {
    response.header("Content-Type","application/json")
    if(request.params.locationId != undefined && request.params.locationId.length > 0 && request.query.shipment_ids != undefined && request.query.shipment_ids.length > 0) {
        var account = getAccount(request);
        if(account != undefined) {
            var shipmentIds = request.query.shipment_ids.split(",")
            var shipments = getShipmentsByIds(account, shipmentIds);
            if(!_.isEmpty(shipments)) {
                response.send(200, {"shipments" : shipments})
            } else {
                var errors = [];
                _.each(shipmentIds, function(shipmentId) {
                    var error = {
                        code : "44013",
                        name : "SHIPMENT_NOT_FOUND_ERROR",
                        message : "The shipment with shipment id d you requested can not be found. Please check the shipment id requested and submit the request again.",
                        field : "shipment_id"
                    }
                    errors.push(error);
                })
                response.send(200, {"shipments" : [], "errors" : errors})
            }
        } else{
            response.send(400,'Unable to find shipment, please check your payload and try again')
        }
    }
})

// List Shipments
Sandmox.define('/shipments/location/:locationId/list?offset=:offset&amount=:amount', 'get', function(request, response) {
    response.header("Content-Type","application/json")

    if(request.params.locationId != undefined && request.params.locationId.length > 0
        && request.params.offset != undefined && request.params.offset.length > 0
        && request.params.amount != undefined && request.params.amount.length > 0) {

        var account = getAccount(request);
        if(account != undefined) {
            var shipments = account.shipments.slice(request.params.offset, request.params.amount);
            response.send(200, {"shipments" : shipments})
        } else{
            response.send(400,'Unable to list shipments, please check your payload and try again')
        }
    }
})

// List Orders
Sandmox.define('/shipments/location/:locationId/order/list?offset=:offset&amount=:amount', 'get', function(request, response) {
    response.header("Content-Type","application/json")

    if(request.params.locationId != undefined && request.params.locationId.length > 0
        && request.params.offset != undefined && request.params.offset.length > 0
        && request.params.amount != undefined && request.params.amount.length > 0) {

        var account = getAccount(request);
        if(account != undefined) {
            var orders = account.orders.slice(request.params.offset, request.params.amount);
            response.send(200, {"orders" : orders})
        } else{
            response.send(400,'Unable to list orders, please check your payload and try again')
        }
    }
})

// Order Summary
Sandmox.define('/shipments/location/:locationId/order/summary/:orderId', 'get', function(request, response) {
    response.header("Content-Type","application/json")
    var account = getAccount(request);
    if(account != undefined) {
        var order = getOrderById(account, request.params.orderId)
        if(order != undefined) {
            var orderDetails = {
                accountNumber: account.id,
                order : order
            }
            response.render('orderSummary', orderDetails);
        } else {
            response.send(404, "Order not found")
        }
    } else{
        response.send(400,'Unable to get order summary, please check your payload and try again')
    }
})

/*
 PRIVATE FUNCTIONS
 */

function createShipments(account, shipments) {
    var newShipments = [];
    _.each(shipments, function(shipment) {
        var newShipment = createShipment(shipment);
        //add the newShipment to the newShipments list
        newShipments.push(newShipment);
        //add new shipment to shipments for account
        account.shipments.push(newShipment);
    });
    return newShipments;
}

function createShipment(shipment) {
    //create new shipment
    var newShipment = {
        shipment_id : shipmentid(),
        shipment_reference: shipment.shipment_reference,
        shipment_creation_date : moment().format(),
        items: [],
        shipment_summary: {
            total_cost: "63.1",
            total_gst: "5.74",
            status: "Created",
            number_of_items: shipment.items.length
        }
    }
    //construct items
    _.each(shipment.items, function(item) {
        var newItem = {
            allow_partial_delivery: item.allow_partial_delivery,
            item_id: itemid(),
            item_reference: item.item_reference,
            product_id: item.product_id,
            item_summary: {
                total_cost: "20.7",
                total_gst: "1.88",
                status: "Created"
            }
        }
        newShipment.items.push(newItem);
    });
    return newShipment;
}

function createOrder(orderReference, shipments) {
    var order = {
        order_id : orderid(),
        order_reference : orderReference,
        order_creation_date : moment().format(),
        order_summary : {
            total_cost: "126.2",
            total_gst: "11.47",
            status: "Initiated",
            number_of_shipments: shipments.length
        },
        shipments : []
    }
    _.each(shipments, function(shipment) {
        var newShipment = {
            shipment_id : !_.isEmpty(shipment.shipment_id) ? shipment.shipment_id : shipmentid,
            shipment_summary: {
                total_cost: "63.1",
                total_gst: "5.74",
                status: "Initiated",
                number_of_items: shipment.items.length
            }
        }
        //add new shipments to shipments for order
        order.shipments.push(newShipment);
    });
    return order;
}

function getShipmentsByIds(account, shipmentIds) {
    var shipments = [];
    if (shipmentIds != undefined) {
        _.each(shipmentIds, function(shipmentId) {
            var shipment = getShipmentById(account, shipmentId);
            if(!_.isEmpty(shipment)) {
                shipments.push(shipment)
            }
        });
    }
    return shipments;
}

function getShipmentById(account, shipmentId) {
    var shipment = {};
    if (shipmentId != undefined) {
        shipment = _.find(_.compact(account.shipments), {shipment_id:shipmentId})
    }
    return shipment;
}

function getOrderById(account, orderId) {
    return _.find(account.orders, {order_id : orderId})
}

function getAccount(request) {
    var accountNumber = getAccountNumber(request);
    if(accountNumber != undefined) {
        //create accounts map if doesn't exist
        state.accounts = state.accounts || [];
        var account = _.find(state.accounts, { id: accountNumber});
        if(account == undefined) {
            account = {
                id: accountNumber,
                orders: [],
                shipments: []
            }
            state.accounts.push(account);
        }
        return account;
    } else {
        return undefined;
    }
}

function getAccountNumber(request) {
    return request.get("account-number");
}

function s4() {
    return Math.floor((1 + Math.random()) * 0x10000)
        .toString(16)
        .substring(1);
};

function itemid() {
    return s4() + s4() + s4() + s4() + s4() + s4()
}

function orderid() {
    return s4() + s4() + s4() + s4()
}

function shipmentid() {
    return s4() + s4() + s4() + s4() + s4() + s4()
}