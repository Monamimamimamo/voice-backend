var socket = new SockJS('https://voice-backend.ru:9003/chat');
var stompClient = Stomp.over(socket);


const headers = {
    Authorization: 'Bearer ' + "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJodHRwOi8vc2NoZW1hcy54bWxzb2FwLm9yZy93cy8yMDA1LzA1L2lkZW50aXR5L2NsYWltcy9uYW1laWRlbnRpZmllciI6IjQ3NjZjOWEwLTc2NTItNGU2Zi05YWZkLWU1NmM0ZDBiMWJhNyIsImh0dHA6Ly9zY2hlbWFzLnhtbHNvYXAub3JnL3dzLzIwMDUvMDUvaWRlbnRpdHkvY2xhaW1zL2VtYWlsYWRkcmVzcyI6ImtpcmlsbDY2NkB5YW5kZXgucnUiLCJodHRwOi8vc2NoZW1hcy54bWxzb2FwLm9yZy93cy8yMDA1LzA1L2lkZW50aXR5L2NsYWltcy9uYW1lIjoiTWlzaGEiLCJodHRwOi8vc2NoZW1hcy5taWNyb3NvZnQuY29tL3dzLzIwMDgvMDYvaWRlbnRpdHkvY2xhaW1zL3JvbGUiOiJBRE1JTiIsImV4cCI6MTcyMjA4NTUzOCwiaXNzIjoiaHR0cHM6Ly9sb2NhbGhvc3Q6NzI2NiIsImF1ZCI6Imh0dHBzOi8vbG9jYWxob3N0OjcyNjYifQ.rhjxmy-HD7G3EAvdl7ZUAT3yVq5YB8e-v4bxMwRD__Y",
};

// Функция для подключения к серверу
function connect() {
    if (!stompClient.connected) {
        stompClient.connect(headers, function(frame) {
            console.log('Connected: ' + frame);
        });
    }
}

// Функция для подписки на канал
function subscribeToChannel() {
    // Проверяем, что поля ввода не пустые
    var userId1 = document.getElementById('userId1').value.trim();
    var userId2 = document.getElementById('userId2').value.trim();

    if (userId1 && userId2) {
        subscribe(userId1, userId2);
    } else {
        alert("Пожалуйста, введите оба ID.");
    }
}

// Функция для подписки на конкретный канал
function subscribe(userId1, userId2) {
    stompClient.subscribe(`/topic/chat/${userId2}/${userId1}`, function(messageOutput) {
        var message = JSON.parse(messageOutput.body);
        displayMessage(message);
    });
}

// Функция для отправки сообщения
function sendMessage(event) {
    event.preventDefault();
    var messageInputDom = document.getElementById('messageInput');
    var message = messageInputDom.value;

    var userId1 = document.getElementById('userId1').value;
    var userId2 = document.getElementById('userId2').value;
    stompClient.send(`/app/chat/${userId1}/${userId2}`, {}, JSON.stringify({'content': message}));
    messageInputDom.value = '';
}

// Функция для отображения сообщения
function displayMessage(message) {
    var messagesDiv = document.getElementById('messages');
    var p = document.createElement('p');
    p.textContent = message.content;
    messagesDiv.appendChild(p);
}


function displayFriendshipMessage(message) {
    var messagesDiv = document.getElementById('messages');
    var p = document.createElement('p');
    p.textContent = `${message.sender} отправил запрос дружбы ${message.receiver} в ${new Date(message.timestamp).toLocaleTimeString()} (${message.status})`;

    // Добавляем кнопки согласия и отказа
    var acceptButton = document.createElement('button');
    acceptButton.textContent = 'Согласиться';
    acceptButton.onclick = function() {
        handleAcceptFriendRequest(message);
    };

    p.appendChild(acceptButton);

    messagesDiv.appendChild(p);
}

connect();