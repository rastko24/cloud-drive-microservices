
import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Router, RouterLink } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrls: ['./login.css']
})
export class LoginComponent {
  email: string = '';
  lozinka: string = '';
  otpKod: string = '';

  prikaziOtp: boolean = false;
  ucitava: boolean = false;
  poruka: string = '';
  greska: string = '';

  private http = inject(HttpClient);
  private router = inject(Router);

  private apiUrl = 'http://localhost:8080/api/users';

  prijaviSe() {
    this.greska = '';
    this.poruka = '';

    if (!this.email.trim() || !this.lozinka) {
      this.greska = 'Unesite e-mail adresu i lozinku.';
      return;
    }

    this.ucitava = true;

    const podaciZaPrijavu = {
      email: this.email.trim(),
      lozinka: this.lozinka
    };

    this.http.post(
      `${this.apiUrl}/login`,
      podaciZaPrijavu,
      { withCredentials: true, responseType: 'text' }
    ).subscribe({
      next: () => {
        this.ucitava = false;
        this.prikaziOtp = true;
        this.lozinka = '';
        this.poruka = 'Kod za potvrdu poslat je na vašu e-mail adresu.';
      },
      error: (odgovor: HttpErrorResponse) => {
        this.ucitava = false;
        this.greska = this.procitajGresku(odgovor);
      }
    });
  }

  proveriOtp() {
    this.greska = '';
    this.poruka = '';

    if (!/^\d{6}$/.test(this.otpKod.trim())) {
      this.greska = 'Unesite šestocifreni kod.';
      return;
    }

    this.ucitava = true;

    const podaciZaOtp = {
      email: this.email.trim(),
      kod: this.otpKod.trim()
    };

    this.http.post(
      `${this.apiUrl}/proveri-otp`,
      podaciZaOtp,
      { withCredentials: true, responseType: 'text' }
    ).subscribe({
      next: () => {
        this.ucitava = false;
        this.router.navigate(['/folderi']);
      },
      error: (odgovor: HttpErrorResponse) => {
        this.ucitava = false;
        this.greska = this.procitajGresku(odgovor);
      }
    });
  }

  nazadNaPrijavu() {
    this.prikaziOtp = false;
    this.otpKod = '';
    this.lozinka = '';
    this.greska = '';
    this.poruka = '';
  }

  private procitajGresku(odgovor: HttpErrorResponse): string {
    if (odgovor.status === 0) {
      return 'Nije moguće povezivanje sa serverom.';
    }

    if (typeof odgovor.error === 'string' && odgovor.error.trim()) {
      return odgovor.error;
    }

    return 'Došlo je do greške. Pokušajte ponovo.';
  }
}
