import { Component, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.html',
})
export class NavbarComponent {
  @Output() newSession = new EventEmitter<void>();

  onNewSession() {
    this.newSession.emit();
  }
}
