
function createLogger(outputId, resultId) {
  var logMessage = {};
  var finished = false;
  logMessage.success = function(message) {
    var pre = $('<p class="logSuccess"></p>');
    pre.text(message);
    $(outputId).append(pre);
  }
  logMessage.failure = function(message) {
    var pre = $('<p class="logFailure"></p>');
    pre.text(message);
    $(outputId).append(pre);
  }
  logMessage.message = function(message) {
    var pre = $('<p class="logMessage"></p>');
    pre.text(message);
    $(outputId).append(pre);
  }
  logMessage.finished = function(result) {
    if (finished) return;
    finished = true;
    console.log("Finished - did we have any success? " + result);
    var msg = "";
    if (result) {
      msg = "Success!";
      $(resultId).addClass("successResult");
    } else {
      msg = "Failure";
      $(resultId).addClass("failureResult");      
    }
    $(resultId).text(msg);
    $(resultId).show();
  }
  return logMessage;
}

function clear(outputId, resultId) {
  $(outputId).empty();
  $(resultId).empty();
  $(outputId).text("");
  $(resultId).text("");
  $(resultId).hide();
}

function reload() {
  location.reload(true);
}

var logger;
var expectClose = false;

function runTest(outputId, resultId, address) {
  console.log("Running tests on " + address + " and outputting to " + outputId + ", final result will be in : " + resultId);
  logger = createLogger(outputId, resultId); 
  var websocket = openWebSocket(address, function() {
    startTests( websocket );
  });
}

function openWebSocket(address, callback) {
  var websocket = new WebSocket(address);
  websocket.onopen = function() {
     logger.success("Successfully opened socket");
     callback();
  };
  websocket.onclose = onClose;
  websocket.onmessage = onMessage;
  websocket.onerror = onError;
  return websocket;
}

function onClose(evt) {
  if (expectClose) { 
    logger.message("Socket closed");
  } else {
    logger.failure("Error: Socket closed");
    logger.failure(JSON.stringify(evt));
    logger.finished(false);
  }
}

function onError(evt) {
    logger.failure("WebSocket error!");
    logger.failure(JSON.stringify(evt));
    logger.finished(false);
}

/* */

var waitFor = {};

function checkResponse( msg ) {
  if (waitFor.hasOwnProperty(msg.subject)) {
    console.log("Triggering callback for '" + msg.subject + "' ...");
    waitFor[msg.subject]( msg );
    //delete waitFor[msg.subject];
  }
}

function waitForResponse( subject, onResponse ) {
  waitFor[subject] = onResponse;
}

function onMessage(evt) {
  var msg = JSON.parse(evt.data);
  logger.message( "RECEIVED: " + JSON.stringify(msg) );
  checkResponse(msg);
}

function sendMessage(websocket, obj) {
  console.log("Sending a message");
  var message = JSON.stringify(obj);
  logger.message("SENT: " + message);
  websocket.send(message);
}

function startTests( websocket ) {
  testHello(websocket, doFailure, function() {
    logger.success("Finished tests");
    logger.finished(true);
  });
}

function doFailure() {
  logger.finished(false);
}

function testHello( websocket , failed, next ) {
  var reg = { 
      "type"   :"This is the type field",
      "from"   :"from - session ID",
      "to"     :"PZP",
      "resp_to":"please reply to .... ",
      "payload": null
  };
  waitForResponse(reg.subject, function(reply) {
    next();
  });
  sendMessage(websocket, reg);  
}

function assert ( test, goodMessage ) {
  if (!test) {
    logger.failure("Failed : " + goodMessage);
  } else {
    logger.success(goodMessage);
  }
  return test;
}


