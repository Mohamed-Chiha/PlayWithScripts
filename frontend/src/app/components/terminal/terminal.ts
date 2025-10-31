import { Component, Input, ElementRef, ViewChild, AfterViewInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Terminal } from 'xterm';
import { FitAddon } from 'xterm-addon-fit';

@Component({
  selector: 'app-terminal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './terminal.html',
})
export class TerminalComponent implements AfterViewInit, OnDestroy {
  @Input() sessionId!: string;
  @ViewChild('terminalContainer', { static: true }) terminalContainer!: ElementRef;

  private term!: Terminal;
  private socket?: WebSocket;
  private fitAddon = new FitAddon();

  ngAfterViewInit() {
    if (!this.terminalContainer?.nativeElement) {
      console.error('❌ Terminal container not found!');
      return;
    }

    this.term = new Terminal({
      cursorBlink: true,
      fontSize: 14,
      theme: { background: '#1e1e1e', foreground: '#ffffff' },
    });

    this.term.loadAddon(this.fitAddon);
    this.term.open(this.terminalContainer.nativeElement);
    this.fitAddon.fit();

    // ✅ Connect WebSocket after terminal is ready
    this.socket = new WebSocket(`ws://localhost:8080/ws/terminal/${this.sessionId}`);

    this.socket.onopen = () => {
      console.log('✅ WebSocket connected to terminal');
    };

    this.socket.onmessage = (event) => {
      this.term.write(event.data);
    };

    // ✅ Send user input to backend (don’t echo locally)
    this.term.onData((data) => {
      if (this.socket?.readyState === WebSocket.OPEN) {
        this.socket.send(data);
      }
    });

    this.socket.onclose = () => {
      this.term.writeln('\r\n--- Session closed ---');
    };

    // Optional: auto-fit on resize
    window.addEventListener('resize', () => this.fitAddon.fit());
  }

  ngOnDestroy() {
    this.socket?.close();
  }
}
