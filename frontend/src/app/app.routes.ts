import { Routes } from '@angular/router';
import { RegisterComponent } from './components/register/register';
import { FoldersComponent } from './components/folders/folders';

export const routes: Routes = [
  { path: '', redirectTo: 'registracija', pathMatch: 'full' },
  { path: 'registracija', component: RegisterComponent },
  { path: 'folderi', component: FoldersComponent }
];