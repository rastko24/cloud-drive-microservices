
import { Routes } from '@angular/router';
import { RegisterComponent } from './components/register/register';
import { FoldersComponent } from './components/folders/folders';
import { LoginComponent } from './components/login/login';
import { ZaboravljenaLozinkaComponent } from './components/zaboravljena-lozinka/zaboravljena-lozinka';
import { ResetLozinkeComponent } from './components/reset-lozinke/reset-lozinke';
import { PromenaLozinkeComponent } from './components/promena-lozinke/promena-lozinke';
import { KvotaComponent } from './components/kvota/kvota';
import { Notifikacije } from './components/notifikacije/notifikacije';
import { MagicLinkPrijavaComponent } from './components/magic-link/magic-link-prijava';
import { MagicLoginComponent } from './components/magic-link/magic-login';

export const routes: Routes = [
  { path: '', redirectTo: 'registracija', pathMatch: 'full' },
  { path: 'registracija', component: RegisterComponent },
  { path: 'login', component: LoginComponent },
  { path: 'magic-link-prijava', component: MagicLinkPrijavaComponent },
  { path: 'magic-login', component: MagicLoginComponent },
  { path: 'zaboravljena-lozinka', component: ZaboravljenaLozinkaComponent },
  { path: 'reset-lozinke', component: ResetLozinkeComponent },
  { path: 'promena-lozinke', component: PromenaLozinkeComponent },
  { path: 'folderi', component: FoldersComponent },
  { path: 'kvota', component: KvotaComponent },
  { path: 'notifikacije', component: Notifikacije }
];
