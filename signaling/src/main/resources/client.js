const socket = new WebSocket('ws://45.141.76.83:9000/socket');

socket.addEventListener('open', function (event) {
    socket.send(JSON.stringify({action: 'share-rooms'}));
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
