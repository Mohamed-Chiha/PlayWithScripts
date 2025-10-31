import { Component } from '@angular/core';
import { CommonModule } from '@angular/common'; // ✅ for *ngIf, etc.
import { Session } from '../../services/session';
import { SessionService } from '../../services/session';

// ✅ import child components
import { NavbarComponent } from '../../components/navbar/navbar';
import { TerminalComponent } from '../../components/terminal/terminal';
import { SessionListComponent } from '../../components/session-list/session-list';

@Component({
  selector: 'app-dashboard',
  standalone: true, // ✅ important for Angular 17+
  imports: [CommonModule, NavbarComponent, TerminalComponent, SessionListComponent],
  templateUrl: './dashboard.html',
})
export class DashboardComponent {
  currentSession?: Session;

  constructor(private sessionService: SessionService) {}

  createSession() {
    this.sessionService.startSession().subscribe((session) => {
      this.currentSession = session;
    });
  }
}
