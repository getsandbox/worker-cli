(function(global) {
  var wrappedRequest = {}
  sandboxValidator(wrappedRequest, __service)

  var wrapCallback = function (callback) {
    return function (callbackRequest, callbackResponse) {
      Object.keys(callbackRequest).forEach(function (property) {
        if (typeof callbackRequest[property] == 'function') {
          wrappedRequest[property] = function () {
            return callbackRequest[property](Array.prototype.slice.call(arguments))
          }
        } else {
          wrappedRequest[property] = callbackRequest[property]
          //clear validation errors for each request
          wrappedRequest._validationErrors = undefined
        }
      })
      return callback(wrappedRequest, callbackResponse)
    }
  }

  var mock = {

    //<path> <method> <properties {}> <callback>
    //<path> <properties {}> <callback>
    //<path> <method> <callback>
    //<path> <callback>
    define: function(){
      var callback;
      var path;
      var method;
      var properties;

      if(arguments.length == 4){
        path = arguments[0];
        method = arguments[1];
        properties = arguments[2];
        callback = arguments[3];

      } else if(arguments.length == 3){
        if(typeof arguments[1] == 'object'){
          path = arguments[0];
          method = 'GET'
          properties = arguments[1];
          callback = arguments[2];
        }else if(typeof arguments[1] == 'string'){
          path = arguments[0];
          method = arguments[1];
          properties = {};
          callback = arguments[2];
        }else{
          throw new Error("Invalid route definition for " + method.toUpperCase() + " " + path + ", 2nd parameter should be a String or an Object")
        }

      } else{
        path = arguments[0];
        method = 'GET';
        properties = {};
        callback = arguments[1];
      }

      if(callback == undefined){
        throw new Error("Invalid route definition for " + method.toUpperCase() + " " + path + ", given function is undefined")
      }

      __mock.define('http', 'define', path, method, properties, callback, wrapCallback(callback), new Error())
    },
    //<path> <action> <callback>
    soap: function(){
      if(arguments.length == 3 && typeof arguments[1] == 'string'){
        __mock.define('http', 'soap', arguments[0], 'POST', {'SOAPAction':arguments[1]}, arguments[2], wrapCallback(arguments[2]), new Error())

      } else if(arguments.length == 4 && typeof arguments[1] == 'string'){
          __mock.define('http', 'soap', arguments[0], 'POST', {'SOAPAction':arguments[1], 'SOAPOperationName':arguments[2]}, arguments[3], wrapCallback(arguments[3]), new Error())

      }else{
        throw new Error("Invalid route definition for " + method.toUpperCase() + " " + path + ", must have 3 parameters (path, action, function)")
      }

    }

  }
  global.Sandbox = mock
  global.Sandmox = mock
  global.mock = mock

})(this)

this.global = this;

module = (typeof module == 'undefined') ? {} :  module;

(function() {

// disable a few things
  loadWithNewGlobal = undefined;

  function Module(id, parent) {
    this.id = id;
    this.parent = parent;
    this.children = [];
    this.filename = id;
    this.loaded = false;
    var self = this;

    Object.defineProperty( this, 'exports', {
      get: function() {
        return this._exports;
      }.bind(this),
      set: function(val) {
        Require.cache[this.filename] = val;
        this._exports = val;
      }.bind(this)
    } );
    this.exports = {};

    if (self.parent && self.parent.children) {
      self.parent.children.push(self);
    }

    self.require = function(id) {
      return Require(id, self);
    };
  }

  Module._load = function(module) {
    if (module.loaded) return;
        var body   = readFile(module.filename),
            funcBody = '(function (exports, require, module, __filename, __dirname) { ' + body + '\n});'

        var func = load({ script: funcBody, name: module.filename })

        func.apply(module, [module.exports, module.require, module, module.filename, module.filename]);

        module.loaded = true;
  };

  function Require(id, parent) {
    var file = Require.resolve(id, parent);

    if (!file) {
      throw new Error("Cannot find module: " + id)
    }

    if (Require.cache[file]) {
      return Require.cache[file];
    } else if(file.endsWith(".json")){
      return JSON.parse(readFile(file));
    } else {
      return loadModule(file, parent);
    }
  }

  Require.resolve = function(id, parent) {
    var rootPath = findRoot(parent);
    var result = resolveAsFile(id, rootPath)
    if (result) {
      return result;
    }
    return false;
  };

  Require.root = "/";

  function findRoot(parent) {
    if (!parent || !parent.id) {
        return Require.root;
    }
    var pathParts = parent.id.split('/');
    pathParts.pop();
    return pathParts.join('/');
  }

  Require.debug = false;
  Require.cache = {};
  Require.extensions = {};
  require = Require;

  module.exports = Module;

  function loadModule(file, parent) {
    var module = new Module(file, parent);
    Module._load(module);
    return module.exports;
  }

  function resolveAsFile(id, root) {

    var filePath
      , pathParts
      , canonicalFilePath

    filePath = root + '/' + id;
    pathParts = [];

    filePath.split('/').forEach(function(part) {
        if (part.startsWith('..')) {
            if (!pathParts.length) {
                // break out, can't go back past root path
                return false;
            }
            pathParts.pop();
        } else if (!part.startsWith('.') && part.length){
            pathParts.push(part)
        }
    })

    canonicalFilePath = pathParts.join('/')
    canonicalFilePath = normalizeName(canonicalFilePath)

    if(__service.hasFile(canonicalFilePath)) {
        return canonicalFilePath;
    } else {
        return false;
    }
  }

  function normalizeName(fileName) {
    if (fileName.endsWith('.js') || fileName.endsWith('.json')) {
      return fileName;
    }else{
      return fileName + '.js';
    }
  }

  function readFile(filename) {
    var fileContents = __service.readFile(filename)

    if (fileContents == null) {
        throw new Error("Cannot get file: " + filename);
    }
    return fileContents;
  }

  // Helper function until ECMAScript 6 is complete
  if (typeof String.prototype.endsWith !== 'function') {
    String.prototype.endsWith = function(suffix) {
      if (!suffix) return false;
      return this.indexOf(suffix, this.length - suffix.length) !== -1;
    };
  }

  if (typeof String.prototype.startsWith != 'function') {
    String.prototype.startsWith = function (str){
      return this.slice(0, str.length) == str;
    };
  }

  if (typeof String.prototype.contains != 'function') {
    String.prototype.contains = String.prototype.includes
  }

  if (typeof Array.prototype.isLength != 'function') {
    Array.prototype.isLength = function isLength(value) {
      return typeof value == 'number' &&
          value > -1 && value % 1 == 0 && value <= 9007199254740991; //MAX_SAFE_INTEGER
    }
  }

}());
