import { Routes } from '@angular/router';
import { Dashboard } from './features/dashboard/dashboard';
import { Terminal } from './core/services/terminal';

export const routes: Routes = [
  { path: '', component: Dashboard },
  { path: 'terminal', component: Terminal },
];
