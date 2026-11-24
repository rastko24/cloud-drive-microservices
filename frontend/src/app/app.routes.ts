
import { Routes } from '@angular/router';
import { RegisterComponent } from './components/register/register';
import { FoldersComponent } from './components/folders/folders';
import { LoginComponent } from './components/login/login';
import { ZaboravljenaLozinkaComponent } from './components/zaboravljena-lozinka/zaboravljena-lozinka';
import { ResetLozinkeComponent } from './components/reset-lozinke/reset-lozinke';
import { PromenaLozinkeComponent } from './components/promena-lozinke/promena-lozinke';
import { KvotaComponent } from './components/kvota/kvota';

export const routes: Routes = [
  { path: '', redirectTo: 'registracija', pathMatch: 'full' },
  { path: 'registracija', component: RegisterComponent },
  { path: 'login', component: LoginComponent },
  { path: 'zaboravljena-lozinka', component: ZaboravljenaLozinkaComponent },
  { path: 'reset-lozinke', component: ResetLozinkeComponent },
  { path: 'promena-lozinke', component: PromenaLozinkeComponent },
  { path: 'folderi', component: FoldersComponent },
  { path: 'kvota', component: KvotaComponent }
];
