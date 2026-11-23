
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-promena-lozinke',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './promena-lozinke.html',
  styleUrls: ['./promena-lozinke.css']
})
export class PromenaLozinkeComponent {
  email = '';
  staraLozinka = '';
  novaLozinka = '';
  potvrdaLozinke = '';
  ucitava = false;
  poruka = '';
  greska = '';

  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8081/api/users/promeni-lozinku';

  constructor() {
    this.email = localStorage.getItem('userEmail') || '';
  }

  promeniLozinku() {
    this.greska = '';
    this.poruka = '';

    if (!this.email.trim() || !this.staraLozinka) {
      this.greska = 'Unesite e-mail i trenutnu lozinku.';
      return;
    }

    if (this.novaLozinka.length < 12 ||
        !/[a-z]/.test(this.novaLozinka) ||
        !/[A-Z]/.test(this.novaLozinka) ||
        !/\d/.test(this.novaLozinka) ||
        !/[^A-Za-z0-9]/.test(this.novaLozinka)) {
      this.greska = 'Nova lozinka mora imati najmanje 12 karaktera, veliko i malo slovo, broj i specijalni znak.';
      return;
    }

    if (this.staraLozinka === this.novaLozinka) {
      this.greska = 'Nova lozinka mora biti različita od stare.';
      return;
    }

    if (this.novaLozinka !== this.potvrdaLozinke) {
      this.greska = 'Nove lozinke se ne poklapaju.';
      return;
    }

    this.ucitava = true;

    this.http.post(
      this.apiUrl,
      {
        email: this.email.trim(),
        staraLozinka: this.staraLozinka,
        novaLozinka: this.novaLozinka
      },
      { withCredentials: true, responseType: 'text' }
    ).subscribe({
      next: () => {
        this.ucitava = false;
        this.poruka = 'Lozinka je uspešno promenjena.';
        this.staraLozinka = '';
        this.novaLozinka = '';
        this.potvrdaLozinke = '';
      },
      error: (odgovor: HttpErrorResponse) => {
        this.ucitava = false;
        this.greska = typeof odgovor.error === 'string' && odgovor.error.trim()
          ? odgovor.error
          : 'Promena lozinke nije uspela.';
      }
    });
  }
}
