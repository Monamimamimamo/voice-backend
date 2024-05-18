var socket = new SockJS('https://voice-backend.ru:8082/chat');
var stompClient = Stomp.over(socket);

// Функция для подключения к серверу
function connect() {
    if (!stompClient.connected) {
        stompClient.connect({}, function(frame) {
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

// Подключаемся к серверу при загрузке страницы
connect();
