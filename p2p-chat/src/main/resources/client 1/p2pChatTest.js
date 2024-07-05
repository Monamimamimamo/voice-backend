
var friendshipSocket = new SockJS('http://localhost:9004/friendship');
var friendshipClient = Stomp.over(friendshipSocket);


const headers = {
    Authorization: 'Bearer ' + "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJodHRwOi8vc2NoZW1hcy54bWxzb2FwLm9yZy93cy8yMDA1LzA1L2lkZW50aXR5L2NsYWltcy9lbWFpbGFkZHJlc3MiOiJraXJpbGw2NjZAeWFuZGV4LnJ1IiwiaHR0cDovL3NjaGVtYXMueG1sc29hcC5vcmcvd3MvMjAwNS8wNS9pZGVudGl0eS9jbGFpbXMvbmFtZSI6IktpcmEiLCJodHRwOi8vc2NoZW1hcy5taWNyb3NvZnQuY29tL3dzLzIwMDgvMDYvaWRlbnRpdHkvY2xhaW1zL3JvbGUiOiJBRE1JTiIsImV4cCI6MTcyMDE1OTU0NywiaXNzIjoiaHR0cHM6Ly9sb2NhbGhvc3Q6NzI2NiIsImF1ZCI6Imh0dHBzOi8vbG9jYWxob3N0OjcyNjYifQ.ZXTaXej3y53zF4-2m6ZnY-QB7GsW-xT0yTtta-lQR2Y",
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
        'sender': headers.Authorization, // Убедитесь, что переменная auth2 содержит правильные данные пользователя
        'status': 'accepted'
    }));
}

function handleRefuseFriendRequest(message) {
    var userId2 = document.getElementById('userId2').value;

    // Отправляем сообщение с отказом в дружбу
    friendshipClient.send(`/app/friendship/${userId2}`, {}, JSON.stringify({
        'timestamp': new Date().getTime(),
        'sender': headers.Authorization, // Убедитесь, что переменная auth2 содержит правильные данные пользователя
        'status': 'refused'
    }));
}

connect();