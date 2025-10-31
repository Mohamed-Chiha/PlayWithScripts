import { bootstrapApplication } from '@angular/platform-browser';
import { routes } from './app/app.routes';
import { provideHttpClient } from '@angular/common/http';
import { App } from './app/app';
import { provideRouter } from '@angular/router';

bootstrapApplication(App, {
  providers: [provideRouter(routes), provideHttpClient()],
}).catch((err) => console.error(err));
