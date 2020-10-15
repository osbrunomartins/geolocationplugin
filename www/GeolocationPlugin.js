cordova.define("com.outsystems.geolocationplugin.GeolocationPlugin", function(require, exports, module) {
    var exec = require('cordova/exec');
    
    exports.coolMethod = function (arg0, success, error) {
        exec(success, error, 'GeolocationPlugin', 'coolMethod', [arg0]);
    };
    
    
    exports.getLocation = function (success, error) {
        exec(success, error, 'GeolocationPlugin', 'Get', [""]);
    };
    
    exports.startCapture = function (time, success, error) {
        exec(success, error, 'GeolocationPlugin', 'Start', ["wait_between", time]);
    };
    
    exports.stopCapture = function (success, error) {
        exec(success, error, 'GeolocationPlugin', 'Stop', [""]);
    };
    
    });
    