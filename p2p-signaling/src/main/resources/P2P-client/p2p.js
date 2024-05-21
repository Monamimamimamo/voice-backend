
const messages = document.getElementById('messages');

let stompClient = null;
const socket = new SockJS('http://localhost:9000/p2p');
stompClient = Stomp.over(socket);

let answerSent = false;
let answerReceived = false;
const configuration = {
 iceServers: [
    { urls: 'stun:stun.l.google.com:19302' },
 ]
};

const peerConnection = new RTCPeerConnection(configuration);

let cond;

function connect() {
    if (!stompClient.connected) {
        stompClient.connect({}, function(frame) {
            console.log('Connected: ' + frame);
        });
    }
}

function subscribeToChannel() {
    var senderId = document.getElementById('senderId').value.trim();
    var receiverId = document.getElementById('receiverId').value.trim();
    if (senderId && receiverId) {
        subscribe(senderId, receiverId);
    } else {
        alert("Пожалуйста, введите оба ID.");
    }
}


async function goVideo() {
     await addMediaStream();
     createOffer();
}

function subscribe(senderId, receiverId) {
    stompClient.subscribe(`/topic/signaling/${receiverId}/${senderId}`, function(messageOutput) {
        var message = JSON.parse(messageOutput.body);
        displayMessage(message);
        switch (message.type) {
             case 'offer':
                 handleOffer(message.offer);
                 break;
             case 'answer':
                 handleAnswer(message.answer);
                 answerReceived = true
                 break;
             case 'candidate':
                 addIceCandidate(message.candidate);
                 break;
            }
    });
}

// Обработчик события icecandidate для обмена кандидатами
peerConnection.onicecandidate = (event) => {
    if (event.candidate) {
        cond = event.candidate;
    }
};

const checkAndSendCandidate = setInterval(() => {
    var senderId = document.getElementById('senderId').value.trim();
    var receiverId = document.getElementById('receiverId').value.trim();
    if (answerSent && answerReceived) {
        if (cond) {
            stompClient.send(`/app/signaling/${senderId}/${receiverId}`, {}, JSON.stringify({ type: 'candidate', candidate: cond }));
            clearInterval(checkAndSendCandidate); // Останавливаем проверку после отправки
        }
    }
}, 1000);

peerConnection.oniceconnectionstatechange = () => {
    console.log('ICE connection state change:', peerConnection.iceConnectionState);
    if (peerConnection.iceConnectionState === 'connected') {
        console.log('ICE candidates successfully registered, connection established');
    }
};




// Обработчик события ontrack для получения медиа-потоков
peerConnection.ontrack = (event) => {
    const remoteVideo = document.getElementById('remoteVideo');
    console.log(event.streams[0]);
    if (remoteVideo) {
        remoteVideo.srcObject = event.streams[0];
    } else {
        console.error('Remote video element not found');
    }
        event.streams.forEach((stream) => {
            stream.getTracks().forEach((track) => {
                console.log(track);
            });
        });
};

// Функция для создания предложения
async function createOffer() {
    var senderId = document.getElementById('senderId').value.trim();
    var receiverId = document.getElementById('receiverId').value.trim();
    const offer = await peerConnection.createOffer();
    await peerConnection.setLocalDescription(offer);
    stompClient.send(`/app/signaling/${senderId}/${receiverId}`, {}, JSON.stringify({ type: 'offer', offer }));
}

// Функция для обработки полученного предложения
async function handleOffer(offer) {
    var senderId = document.getElementById('senderId').value.trim();
    var receiverId = document.getElementById('receiverId').value.trim();
    await peerConnection.setRemoteDescription(offer);
    const answer = await peerConnection.createAnswer();
    await peerConnection.setLocalDescription(answer);
    stompClient.send(`/app/signaling/${senderId}/${receiverId}`, {}, JSON.stringify({ type: 'answer', answer }));
    answerSent = true
}

// Функция для обработки полученного ответа
async function handleAnswer(answer) {
    await peerConnection.setRemoteDescription(answer);
}

// Функция для добавления медиа-потока
async function addMediaStream() {
    try {
        const stream = await navigator.mediaDevices.getUserMedia({ video: true, audio: true });
        stream.getTracks().forEach(track => peerConnection.addTrack(track, stream));
    } catch (error) {
        console.error('Error adding media stream:', error);
    }
}

function addIceCandidate(candidate) {
    peerConnection.addIceCandidate(new RTCIceCandidate(candidate));
}

function displayMessage(message) {
    var messagesDiv = document.getElementById('messages');
    var p = document.createElement('p');
    p.textContent = message.content;
    messagesDiv.appendChild(p);
}


connect();