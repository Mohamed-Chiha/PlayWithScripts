import { Component, EventEmitter, Output, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common'; // ✅ needed for *ngFor, slice, etc.
import { Session, SessionService } from '../../services/session';

@Component({
  selector: 'app-session-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './session-list.html',
})
export class SessionListComponent implements OnInit {
  sessions: Session[] = [];
  @Output() select = new EventEmitter<Session>();

  constructor(private sessionService: SessionService) {}

  ngOnInit() {
    this.refresh();
  }

  refresh() {
    this.sessionService.listSessions().subscribe((data) => (this.sessions = data));
  }
}
