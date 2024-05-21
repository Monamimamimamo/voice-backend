var socket = new SockJS('http://localhost:9001/chat');
var friendshipSocket = new SockJS('http://localhost:9898/friendship');
var stompClient = Stomp.over(socket);
var friendshipClient = Stomp.over(friendshipSocket);

// Функция для подключения к серверу
function connect() {
    if (!stompClient.connected) {
        stompClient.connect({}, function(frame) {
            console.log('Connected: ' + frame);
        });
    }
        if (!friendshipClient.connected) {
            friendshipClient.connect({}, function(frame) {
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
