const startButton = document.getElementById('start');
const messages = document.getElementById('messages');
let answerSent = false;
let answerReceived = false;
const configuration = {
 iceServers: [
    { urls: 'stun:stun.l.google.com:19302' },
 ]
};

const peerConnection = new RTCPeerConnection(configuration);

let cond;

// Подключаемся к WebSocket серверу
const ws = new WebSocket('wss://voice-backend.ru:8082/p2p');


// Обработчик события icecandidate для обмена кандидатами
peerConnection.onicecandidate = (event) => {
    if (event.candidate) {
        cond = event.candidate;
    }
};

const checkAndSendCandidate = setInterval(() => {
    if (answerSent && answerReceived) {
        if (cond) {
            ws.send(JSON.stringify({ type: 'candidate', candidate: cond }));
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


ws.onmessage = (event) => {
    const message = JSON.parse(event.data);
    if (message.type === 'offer') {
        handleOffer(message.offer);
    } else if (message.type === 'answer') {
        handleAnswer(message.answer);
        answerReceived = true
    } else if (message.type === 'candidate') {
        addIceCandidate(message.candidate);
    }
};

// Обработчик события ontrack для получения медиа-потоков
peerConnection.ontrack = (event) => {
    const remoteVideo = document.getElementById('remoteVideo');
    if (remoteVideo) {
        remoteVideo.srcObject = event.streams[0];
    } else {
        console.error('Remote video element not found');
    }
};

// Функция для создания предложения
async function createOffer() {
    const offer = await peerConnection.createOffer();
    await peerConnection.setLocalDescription(offer);
    ws.send(JSON.stringify({ type: 'offer', offer }));
}

// Функция для обработки полученного предложения
async function handleOffer(offer) {
    await peerConnection.setRemoteDescription(offer);
    const answer = await peerConnection.createAnswer();
    await peerConnection.setLocalDescription(answer);
    ws.send(JSON.stringify({ type: 'answer', answer }));
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

// Функция для добавления ICE кандидата
function addIceCandidate(candidate) {
    peerConnection.addIceCandidate(new RTCIceCandidate(candidate));
}

// Обработчик события нажатия кнопки
startButton.addEventListener('click', async () => {
    await addMediaStream();
    createOffer();
});
