var socket = new SockJS('https://voice-backend.ru:8082/chat');
var friendshipSocket = new SockJS('https://voice-backend.ru:8082/friendship');
var stompClient = Stomp.over(socket);
var friendshipClient = Stomp.over(friendshipSocket);


const headers = {
    Authorization: 'Bearer ' + "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJodHRwOi8vc2NoZW1hcy54bWxzb2FwLm9yZy93cy8yMDA1LzA1L2lkZW50aXR5L2NsYWltcy9lbWFpbGFkZHJlc3MiOiJTaHVrc2hpbm1ha3NpbS5ydUBtYWlsLnJ1IiwiaHR0cDovL3NjaGVtYXMueG1sc29hcC5vcmcvd3MvMjAwNS8wNS9pZGVudGl0eS9jbGFpbXMvbmFtZSI6IlNodWtzaGlubWFrc2ltLnJ1QG1haWwucnUiLCJodHRwOi8vc2NoZW1hcy5taWNyb3NvZnQuY29tL3dzLzIwMDgvMDYvaWRlbnRpdHkvY2xhaW1zL3JvbGUiOiJBZG1pbiIsImV4cCI6MTcxNjIzMTM3MCwiaXNzIjoiaHR0cHM6Ly9sb2NhbGhvc3Q6NzI2NiIsImF1ZCI6Imh0dHBzOi8vbG9jYWxob3N0OjcyNjYifQ.gl6KyviYyB8hNAQ5fjZ_vGjlR8w4koJjfqHumoSw5m8",
};

// Функция для подключения к серверу
function connect() {
    if (!stompClient.connected) {
        stompClient.connect(headers, function(frame) {
            console.log('Connected: ' + frame);
        });
    }
        if (!friendshipClient.connected) {
            friendshipClient.connect(headers, function(frame) {
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

    friendshipClient.subscribe(`/topic/friendship/${userId1}`, function(friendshipMessage) {
        var message = JSON.parse(friendshipMessage.body);
        displayFriendshipMessage(message);
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

function sendFriendRequest() {
    var userId1 = document.getElementById('userId1').value.trim();
    var userId2 = document.getElementById('userId2').value.trim();

    if (userId1 && userId2) {

        // Отправляем сообщение с запросом дружбы
        var timestamp = new Date().getTime(); // Генерируем текущее время в миллисекундах
        friendshipClient.send(`/app/friendship/${userId2}`, {}, JSON.stringify({
            'timestamp': timestamp,
            'sender': userId1,
            'receiver': userId2,
            'status': 'pending'
        }));
    } else {
        alert("Пожалуйста, введите оба ID.");
    }
}

function displayFriendshipMessage(message) {
    var messagesDiv = document.getElementById('messages');
    var p = document.createElement('p');
    p.textContent = `${message.sender} отправил запрос дружбы ${message.receiver} в ${new Date(message.timestamp).toLocaleTimeString()} (${message.status})`;
    messagesDiv.appendChild(p);
}

// Подключаемся к серверу при загрузке страницы
connect();
