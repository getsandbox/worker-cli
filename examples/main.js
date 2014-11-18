/**
 * My API Sandbox
 * 
 */

// A basic route returning a canned response
Sandbox.define('/test', 'get', function(req, res) {
    res.send('Hello world');
});


// A route using stateful behaviour to simulate adding users to a list
Sandbox.define('/users', 'POST', function(req, res) {
    // retrieve users or if there are none init to empty array
    state.users = state.users || [];
    
    // persist user by adding to the state object
    state.users.push(req.body);

    return res.json({status: "ok"});
});

// A route using stateful behaviour to simulate getting all added users 
Sandbox.define('/users', 'GET', function(req, res) {
    // retrieve users or if there are none init to empty array
    state.users = state.users || [];

    return res.json(state.users);
});

// A route using stateful behaviour to simulate getting all added users 
Sandbox.define('/users/{username}', 'GET', function(req, res) {
    // retrieve users or if there are none init to empty array
    state.users = state.users || [];
    // get the username from req.params
    var username = req.params.username;
    // use lodash to find the user in the array
    var user = _.find(state.users, { "username": username});
    
    return res.json(user);
});