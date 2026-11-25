
import {
  ChangeDetectorRef,
  Component,
  inject,
  OnInit
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  HttpClient,
  HttpErrorResponse
} from '@angular/common/http';
import { Router, RouterLink } from '@angular/router';
import { APP_CONFIG } from '../../app.config';

interface PrijavljeniKorisnik {
  id: string;
  email: string;
  ime: string;
  prezime: string;
  kvotaSkladista: number;
  zauzetoSkladiste: number;
}

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrls: ['./login.css']
})
export class LoginComponent implements OnInit {

  vecUlogovan = false;
  ulogovanKorisnik = '';

  email = '';
  lozinka = '';
  otpKod = '';

  prikaziOtpPopup = false;
  ucitava = false;

  poruka = '';
  greska = '';
  otpGreska = '';

  private http = inject(HttpClient);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);

  private apiUrl = `${APP_CONFIG.gatewayApiUrl}/api/users`;

  ngOnInit() {
    this.http.get<PrijavljeniKorisnik>(
      `${this.apiUrl}/me`,
      { withCredentials: true }
    ).subscribe({
      next: korisnik => {
        this.sacuvajKorisnika(korisnik);
        this.cdr.detectChanges();
      },
      error: () => {
        this.ocistiLokalnuPrijavu();
        this.cdr.detectChanges();
      }
    });
  }

  private sacuvajKorisnika(korisnik: PrijavljeniKorisnik) {
    localStorage.setItem('userId', korisnik.id);
    localStorage.setItem('userEmail', korisnik.email);

    this.vecUlogovan = true;
    this.ulogovanKorisnik = korisnik.email;
  }

  private ocistiLokalnuPrijavu() {
    localStorage.removeItem('userId');
    localStorage.removeItem('userEmail');

    this.vecUlogovan = false;
    this.ulogovanKorisnik = '';
  }

  idiNaFoldere() {
    this.router.navigate(['/folderi']);
  }

  odjaviSe() {
    this.http.post(
      `${this.apiUrl}/logout`,
      {},
      {
        withCredentials: true,
        responseType: 'text'
      }
    ).subscribe({
      next: () => {
        this.ocistiLokalnuPrijavu();
        this.cdr.detectChanges();
      },
      error: (odgovor: HttpErrorResponse) => {
        this.greska = this.procitajGresku(odgovor);
        this.cdr.detectChanges();
      }
    });
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

    this.http.post(
      `${this.apiUrl}/login`,
      {
        email: this.email.trim(),
        lozinka: this.lozinka
      },
      {
        withCredentials: true,
        responseType: 'text'
      }
    ).subscribe({
      next: () => {
        this.ucitava = false;
        this.lozinka = '';
        this.otpKod = '';
        this.otpGreska = '';

        this.poruka =
          `Kod za potvrdu poslat je na ${this.email.trim()}.`;

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

    this.http.post(
      `${this.apiUrl}/proveri-otp`,
      {
        email: this.email.trim(),
        kod: this.otpKod.trim()
      },
      {
        withCredentials: true,
        responseType: 'text'
      }
    ).subscribe({
      next: () => {
        this.ucitajPrijavljenogKorisnika();
      },
      error: (odgovor: HttpErrorResponse) => {
        this.ucitava = false;
        this.otpGreska = this.procitajGresku(odgovor);
        this.cdr.detectChanges();
      }
    });
  }

  private ucitajPrijavljenogKorisnika() {
    this.http.get<PrijavljeniKorisnik>(
      `${this.apiUrl}/me`,
      { withCredentials: true }
    ).subscribe({
      next: korisnik => {
        this.sacuvajKorisnika(korisnik);

        this.ucitava = false;
        this.prikaziOtpPopup = false;
        this.otpKod = '';

        this.cdr.detectChanges();
        this.router.navigate(['/folderi']);
      },
      error: (odgovor: HttpErrorResponse) => {
        this.ucitava = false;

        this.otpGreska =
          'OTP je prihvaćen, ali podaci korisnika nisu učitani. ' +
          this.procitajGresku(odgovor);

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
      {
        withCredentials: true,
        responseType: 'text'
      }
    ).subscribe({
      next: odgovor => {
        this.ucitava = false;

        this.poruka = odgovor ||
          `Novi kod je poslat na ${this.email.trim()}.`;

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

  private procitajGresku(
    odgovor: HttpErrorResponse
  ): string {

    if (odgovor.status === 0) {
      return 'Nije moguće povezivanje sa serverom.';
    }

    if (
      typeof odgovor.error === 'string' &&
      odgovor.error.trim()
    ) {
      return odgovor.error;
    }

    return 'Došlo je do greške. Pokušajte ponovo.';
  }
}
