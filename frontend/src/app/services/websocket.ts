import { Injectable, NgZone } from '@angular/core';
import { Subject } from 'rxjs';

const HEARTBEAT_INTERVAL = 25000; // 25 seconds

@Injectable({ providedIn: 'root' })
export class WebSocketService {
  private socket?: WebSocket;
  private heartbeatInterval?: any;
  public messages = new Subject<string>();

  constructor(private zone: NgZone) {}

  connect(sessionId: string) {
    const wsUrl = `ws://localhost:8080/ws/terminal/${sessionId}`;
    this.socket = new WebSocket(wsUrl);

    this.socket.onopen = () => {
      console.log('[WebSocket] Connected');

      // 🟢 Start sending heartbeat pings
      this.heartbeatInterval = setInterval(() => {
        if (this.socket?.readyState === WebSocket.OPEN) {
          this.socket.send('__ping__');
        }
      }, HEARTBEAT_INTERVAL);
    };

    this.socket.onmessage = (event) => {
      this.zone.run(() => this.messages.next(event.data));
    };

    this.socket.onclose = (event) => {
      console.log('[WebSocket] Closed:', event.reason);

      // 🔴 Stop heartbeats when the connection closes
      if (this.heartbeatInterval) {
        clearInterval(this.heartbeatInterval);
        this.heartbeatInterval = undefined;
      }
    };

    this.socket.onerror = (error) => {
      console.error('[WebSocket] Error:', error);
    };
  }

  send(input: string) {
    // Don’t send if socket is closed or not ready
    if (this.socket?.readyState === WebSocket.OPEN) {
      this.socket.send(input);
    }
  }

  close() {
    if (this.heartbeatInterval) {
      clearInterval(this.heartbeatInterval);
      this.heartbeatInterval = undefined;
    }
    this.socket?.close();
  }
}
