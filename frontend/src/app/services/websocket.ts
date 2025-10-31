import { Injectable, NgZone } from '@angular/core';
import { Subject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class WebSocketService {
  private socket?: WebSocket;
  public messages = new Subject<string>();

  constructor(private zone: NgZone) {}

  connect(sessionId: string) {
    const wsUrl = `ws://localhost:8080/ws/terminal/${sessionId}`;
    this.socket = new WebSocket(wsUrl);

    this.socket.onmessage = (event) => {
      this.zone.run(() => this.messages.next(event.data));
    };

    this.socket.onclose = (event) => {
      console.log('WebSocket closed:', event.reason);
    };
  }

  send(input: string) {
    this.socket?.send(input);
  }

  close() {
    this.socket?.close();
  }
}
