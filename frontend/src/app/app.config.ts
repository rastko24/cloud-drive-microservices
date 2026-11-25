import { ApplicationConfig } from '@angular/core';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';

export interface AppConfig {
  gatewayApiUrl: string;
  usersApiUrl: string;
  filesApiUrl: string;
  notificationsApiUrl: string;
}

export const APP_CONFIG: AppConfig = {
  gatewayApiUrl: 'http://localhost:8080',
  usersApiUrl: 'http://localhost:8080',
  filesApiUrl: 'http://localhost:8080',
  notificationsApiUrl: 'http://localhost:8080/api/notifikacije',
};

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(),
  ],
};