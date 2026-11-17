
import { Routes } from '@angular/router';
import { RegisterComponent } from './components/register/register';
import { FoldersComponent } from './components/folders/folders';
import { LoginComponent } from './components/login/login';

export const routes: Routes = [
  { path: '', redirectTo: 'registracija', pathMatch: 'full' },
  { path: 'registracija', component: RegisterComponent },
  { path: 'login', component: LoginComponent },
  { path: 'folderi', component: FoldersComponent }
];
