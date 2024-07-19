var friendshipSocket = new SockJS('https://voice-backend.ru:9004/friendship');
var friendshipClient = Stomp.over(friendshipSocket);


const headers = {
    Authorization: 'Bearer ' + "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJodHRwOi8vc2NoZW1hcy54bWxzb2FwLm9yZy93cy8yMDA1LzA1L2lkZW50aXR5L2NsYWltcy9lbWFpbGFkZHJlc3MiOiJraXJpbGw2NjI2QHlhbmRleC5ydSIsImh0dHA6Ly9zY2hlbWFzLnhtbHNvYXAub3JnL3dzLzIwMDUvMDUvaWRlbnRpdHkvY2xhaW1zL25hbWUiOiJNaXNoYSIsImh0dHA6Ly9zY2hlbWFzLm1pY3Jvc29mdC5jb20vd3MvMjAwOC8wNi9pZGVudGl0eS9jbGFpbXMvcm9sZSI6IkFETUlOIiwiZXhwIjoxNzIxMTQ3MDgxLCJpc3MiOiJodHRwczovL2xvY2FsaG9zdDo3MjY2IiwiYXVkIjoiaHR0cHM6Ly9sb2NhbGhvc3Q6NzI2NiJ9.8sdIu50m845-mr4RJ1fq5X2VEFfvo1E51ClgE8vggTM",
};

// Функция для подключения к серверу
function connect() {
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
        subscribe(userId1);
    } else {
        alert("Пожалуйста, введите оба ID.");
    }
}

// Функция для подписки на конкретный канал
function subscribe(userId1) {

    friendshipClient.subscribe(`/topic/friendship/${userId1}`, function(friendshipMessage) {
        var message = JSON.parse(friendshipMessage.body);
        displayFriendshipMessage(message);
    });
}



// Функция для отображения сообщения
function displayMessage(message) {
    var messagesDiv = document.getElementById('messages');
    var p = document.createElement('p');
    p.textContent = message.content;
    messagesDiv.appendChild(p);
}

function sendFriendRequest() {
    var userId2 = document.getElementById('userId2').value.trim();

    if (userId2) {

        // Отправляем сообщение с запросом дружбы
        var timestamp = new Date().getTime(); // Генерируем текущее время в миллисекундах
        friendshipClient.send(`/app/friendship/${userId2}`, {}, JSON.stringify({
            'timestamp': timestamp,
            'sender': headers.Authorization,
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

    // Создаем кнопку "Согласиться"
    var acceptButton = document.createElement('button');
    acceptButton.textContent = 'Согласиться';
    acceptButton.onclick = function() {
        handleAcceptFriendRequest(message);
        p.removeChild(acceptButton);
    };

    var refuseButton = document.createElement('button');
    refuseButton.textContent = 'Отказаться';
    refuseButton.onclick = function() {
        handleRefuseFriendRequest(message);
        p.removeChild(refuseButton);
    };

    p.appendChild(acceptButton);
    p.appendChild(refuseButton);

    messagesDiv.appendChild(p);
}

function handleAcceptFriendRequest(message) {
    var userId2 = document.getElementById('userId2').value;

    // Отправляем сообщение с подтверждением дружбы
    friendshipClient.send(`/app/friendship/${userId2}`, {}, JSON.stringify({
        'timestamp': new Date().getTime(),
        'sender': headers.Authorization,
        'status': 'accepted'
    }));
}

function handleRefuseFriendRequest(message) {
    var userId2 = document.getElementById('userId2').value;

    // Отправляем сообщение с отказом в дружбу
    friendshipClient.send(`/app/friendship/${userId2}`, {}, JSON.stringify({
        'timestamp': new Date().getTime(),
        'sender': headers.Authorization,
        'status': 'refused'
    }));
}

connect();