import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms'; // ✅ Add this

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [FormsModule], // ✅ Include here
  templateUrl: './settings.html',
  styleUrls: ['./settings.scss'],
})
export class SettingsComponent {
  user = {
    name: 'Mohamed Chiha',
    email: 'mohamed.chiha@example.com',
    plan: 'Free Tier',
  };

  saveChanges() {
    console.log('User updated:', this.user);
  }
}
