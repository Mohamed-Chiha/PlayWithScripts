import { Routes } from '@angular/router';
import { DashboardComponent } from './pages/dashboard/dashboard';
import { SettingsComponent } from './pages/settings/settings';

export const routes: Routes = [
  // Default home
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },

  // Main pages
  { path: 'dashboard', component: DashboardComponent },
  { path: 'settings', component: SettingsComponent },

  // Catch-all redirect (if user types an invalid URL)
  { path: '**', redirectTo: 'dashboard' },
];
