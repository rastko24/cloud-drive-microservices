import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { APP_CONFIG } from '../../app.config';

@Component({
  selector: 'app-reset-lozinke',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './reset-lozinke.html',
  styleUrls: ['./reset-lozinke.css']
})
export class ResetLozinkeComponent {
  email = '';
  token = '';
  novaLozinka = '';
  potvrdaLozinke = '';
  ucitava = false;
  uspesno = false;
  greska = '';

  private http = inject(HttpClient);
  private ruta = inject(ActivatedRoute);
  private cdr = inject(ChangeDetectorRef);
  private apiUrl = `${APP_CONFIG.usersApiUrl}/api/users/resetuj-lozinku`;

  constructor() {
    this.email = this.ruta.snapshot.queryParamMap.get('email') || '';
    this.token = this.ruta.snapshot.queryParamMap.get('token') || '';
  }

  resetujLozinku() {
    this.greska = '';

    if (!this.email || !this.token) {
      this.greska = 'Link za resetovanje nije ispravan.';
      return;
    }

    if (!this.proveriLozinku(this.novaLozinka)) {
      this.greska = 'Lozinka mora imati najmanje 12 karaktera, veliko i malo slovo, broj i specijalni znak.';
      return;
    }

    if (this.novaLozinka !== this.potvrdaLozinke) {
      this.greska = 'Lozinke se ne poklapaju.';
      return;
    }

    this.ucitava = true;

    this.http.post(
      this.apiUrl,
      {
        email: this.email,
        token: this.token,
        novaLozinka: this.novaLozinka
      },
      { responseType: 'text' }
    ).subscribe({
      next: () => {
        this.ucitava = false;
        this.uspesno = true;
        this.novaLozinka = '';
        this.potvrdaLozinke = '';
        this.token = '';
        this.cdr.detectChanges();
      },
      error: (odgovor: HttpErrorResponse) => {
        this.ucitava = false;
        this.greska = typeof odgovor.error === 'string' && odgovor.error.trim()
          ? odgovor.error
          : 'Resetovanje nije uspelo. Link je možda istekao.';
        this.cdr.detectChanges();
      }
    });
  }

  private proveriLozinku(lozinka: string): boolean {
    return lozinka.length >= 12 &&
      /[a-z]/.test(lozinka) &&
      /[A-Z]/.test(lozinka) &&
      /\d/.test(lozinka) &&
      /[^A-Za-z0-9]/.test(lozinka);
  }
}