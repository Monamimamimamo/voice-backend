const socket = new WebSocket('ws://45.141.76.83:8082/signaling/socket');

socket.addEventListener('open', function (event) {
    console.log('Connected to server');
});

socket.addEventListener('message', function (event) {
    console.log('Message from server: ', event.data);
});


document.getElementById('join').addEventListener('click', function() {
    socket.send(JSON.stringify({action: 'join', room: 'room1'}));
});

document.getElementById('leave').addEventListener('click', function() {
    socket.send(JSON.stringify({action: 'leave', room: 'room1'}));
});
