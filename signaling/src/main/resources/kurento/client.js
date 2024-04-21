const socket = io('http://localhost:9898');

document.getElementById('create').addEventListener('click', () => {
    socket.emit('create', { sdpOffer: "ваш SDP-оффер здесь" });
});

document.getElementById('join').addEventListener('click', () => {
    socket.emit('join', { /* данные для присоединения к конференции */ });
});

document.getElementById('leave').addEventListener('click', () => {
    socket.emit('leave', { /* данные для отключения от конференции */ });
});

socket.on('createResponse', (data) => {
    // Обработка ответа от сервера после создания конференции
});

socket.on('joinResponse', (data) => {
    // Обработка ответа от сервера после присоединения к конференции
});

socket.on('leaveResponse', (data) => {
    // Обработка ответа от сервера после отключения от конференции
});
