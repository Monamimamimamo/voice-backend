var socket = new SockJS('http://localhost:9000/ws');
var stompClient = Stomp.over(socket);

function connect() {
    stompClient.connect({}, function(frame) {
        console.log('Connected: ' + frame);
        subscribe();
    });
}

function subscribe() {
    stompClient.subscribe('/topic/chat/userId1/userId2', function(messageOutput) {
        var message = JSON.parse(messageOutput.body);
        displayMessage(message);
    });
}

function sendMessage(event) {
    event.preventDefault();
    var messageInputDom = document.getElementById('messageInput');
    var message = messageInputDom.value;
    stompClient.send("/app/chat/userId1/userId2", {}, JSON.stringify({'content': message}));
    messageInputDom.value = '';
}

function displayMessage(message) {
    var messagesDiv = document.getElementById('messages');
    var p = document.createElement('p');
    p.textContent = message.content;
    messagesDiv.appendChild(p);
}

connect();
