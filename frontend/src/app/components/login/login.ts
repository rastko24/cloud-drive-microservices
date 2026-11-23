import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
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
export class LoginComponent implements OnInit {
  vecUlogovan: boolean = false;
  ulogovanKorisnik: string = '';
  email: string = '';
  lozinka: string = '';
  otpKod: string = '';

  prikaziOtpPopup: boolean = false;
  ucitava: boolean = false;
  poruka: string = '';
  greska: string = '';
  otpGreska: string = '';

  private http = inject(HttpClient);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);

  private apiUrl = 'http://localhost:8081/api/users';

  ngOnInit() {
    const email = localStorage.getItem('userEmail');
    const id = localStorage.getItem('userId');
    if (email || id) {
      this.vecUlogovan = true;
      this.ulogovanKorisnik = email || id || '';
    }
  }

  idiNaFoldere() {
    this.router.navigate(['/folderi']);
  }

  odjaviSe() {
    localStorage.removeItem('userId');
    localStorage.removeItem('userEmail');
    this.vecUlogovan = false;
    this.ulogovanKorisnik = '';
    this.cdr.detectChanges();
  }

  prijaviSe() {
    this.greska = '';
    this.poruka = '';
    this.otpGreska = '';

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
        this.lozinka = '';
        this.otpKod = '';
        this.otpGreska = '';
        this.poruka = `Kod za potvrdu poslat je na ${this.email.trim()}.`;
        this.prikaziOtpPopup = true;
        this.cdr.detectChanges();
      },
      error: (odgovor: HttpErrorResponse) => {
        this.ucitava = false;
        this.greska = this.procitajGresku(odgovor);
        this.cdr.detectChanges();
      }
    });
  }

  proveriOtp() {
    this.otpGreska = '';

    if (!/^\d{6}$/.test(this.otpKod.trim())) {
      this.otpGreska = 'Unesite šestocifreni kod.';
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
      next: (odgovor) => {
        this.ucitava = false;
        this.prikaziOtpPopup = false;
        const cleanEmail = this.email.trim();
        localStorage.setItem('userEmail', cleanEmail);
        try {
          const parsed = typeof odgovor === 'string' ? JSON.parse(odgovor) : odgovor;
          const id = parsed?.userId || parsed?.id;
          if (id) {
            localStorage.setItem('userId', String(id));
          }
        } catch {
        }
        this.cdr.detectChanges();
        this.router.navigate(['/folderi']);
      },
      error: (odgovor: HttpErrorResponse) => {
        this.ucitava = false;
        this.otpGreska = this.procitajGresku(odgovor);
        this.cdr.detectChanges();
      }
    });
  }

  posaljiOtpPonovo() {
    this.otpGreska = '';
    this.ucitava = true;

    this.http.post(
      `${this.apiUrl}/ponovo-posalji-kod`,
      { email: this.email.trim() },
      { withCredentials: true, responseType: 'text' }
    ).subscribe({
      next: (odgovor) => {
        this.ucitava = false;
        this.poruka = odgovor || `Novi kod je poslat na ${this.email.trim()}.`;
        this.cdr.detectChanges();
      },
      error: (odgovor: HttpErrorResponse) => {
        this.ucitava = false;
        this.otpGreska = this.procitajGresku(odgovor);
        this.cdr.detectChanges();
      }
    });
  }

  zatvoriOtpPopup() {
    this.prikaziOtpPopup = false;
    this.otpKod = '';
    this.otpGreska = '';
    this.cdr.detectChanges();
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