import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Router, RouterLink } from '@angular/router';

interface KvotaPregled {
  kvotaSkladista: number;
  zauzetoSkladiste: number;
  slobodnoProstora: number;
  admin: boolean;
}

interface ZahtevKvota {
  id: string;
  korisnikId: string;
  emailKorisnika: string;
  zeljenaKvota: number;
  status: string;
  kreiran: string;
}

@Component({
  selector: 'app-kvota',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './kvota.html',
  styleUrls: ['./kvota.css']
})
export class KvotaComponent implements OnInit {
  kvotaSkladista = 0;
  zauzetoSkladiste = 0;
  slobodnoProstora = 0;
  jeAdmin = false;

  zeljenaKvota = '';
  ucitava = false;
  greska = '';
  poruka = '';

  zahtevi: ZahtevKvota[] = [];

  private http = inject(HttpClient);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);
  private apiUrl = 'http://localhost:8080/api/users';

  ngOnInit() {
    const ulogovan =
      localStorage.getItem('userId') ||
      localStorage.getItem('userEmail') ||
      '';

    if (!ulogovan) {
      alert('Prvo se prijavite na /login.');
      this.router.navigate(['/login']);
      return;
    }

    this.osveziKvotu();
  }

  osveziKvotu() {
    this.greska = '';
    this.ucitava = true;

    this.http.get<KvotaPregled>(`${this.apiUrl}/kvota`, { withCredentials: true }).subscribe({
      next: (podaci) => {
        this.kvotaSkladista = podaci.kvotaSkladista ?? 0;
        this.zauzetoSkladiste = podaci.zauzetoSkladiste ?? 0;
        this.slobodnoProstora = podaci.slobodnoProstora ?? 0;
        this.jeAdmin = !!podaci.admin;
        this.ucitava = false;

        if (this.jeAdmin) {
          this.ucitajZahteve();
        } else {
          this.zahtevi = [];
        }

        this.cdr.detectChanges();
      },
      error: (e: HttpErrorResponse) => {
        this.ucitava = false;
        if (e.status === 401 || e.status === 403) {
          this.router.navigate(['/login']);
          return;
        }
        this.greska = this.procitajGresku(e, 'Nije moguce ucitati kvotu.');
        this.cdr.detectChanges();
      }
    });
  }

  posaljiZahtev() {
    this.greska = '';
    this.poruka = '';

    const mb = Number(this.zeljenaKvota.trim().replace(',', '.'));
    if (!Number.isFinite(mb) || mb <= 0) {
      this.greska = 'Unesite novu ukupnu kvotu u MB (broj veći od 0).';
      return;
    }

    const broj = Math.round(mb * 1024 * 1024);

    if (broj <= this.kvotaSkladista) {
      this.greska = 'Nova kvota mora biti veca od trenutne.';
      return;
    }

    this.ucitava = true;

    this.http.post(
      `${this.apiUrl}/zahtev-kvota`,
      { zeljenaKvota: broj },
      { withCredentials: true }
    ).subscribe({
      next: () => {
        this.ucitava = false;
        this.zeljenaKvota = '';
        this.poruka = 'Zahtev je sacuvan i ceka obradu.';
        this.cdr.detectChanges();
      },
      error: (e: HttpErrorResponse) => {
        this.ucitava = false;
        this.greska = this.procitajGresku(e, 'Zahtev nije poslat.');
        this.cdr.detectChanges();
      }
    });
  }

  ucitajZahteve() {
    this.http.get<ZahtevKvota[]>(`${this.apiUrl}/admin/zahtevi-kvota`, { withCredentials: true }).subscribe({
      next: (lista) => {
        this.zahtevi = lista ?? [];
        this.cdr.detectChanges();
      },
      error: (e: HttpErrorResponse) => {
        this.greska = this.procitajGresku(e, 'Nije moguce ucitati zahteve.');
        this.cdr.detectChanges();
      }
    });
  }

  odobri(id: string) {
    this.http.post(
      `${this.apiUrl}/admin/zahtevi-kvota/${encodeURIComponent(id)}/odobri`,
      {},
      { withCredentials: true, responseType: 'text' }
    ).subscribe({
      next: () => {
        this.poruka = 'Zahtev je odobren.';
        this.osveziKvotu();
      },
      error: (e: HttpErrorResponse) => {
        this.greska = this.procitajGresku(e, 'Odobravanje nije uspelo.');
        this.cdr.detectChanges();
      }
    });
  }

  odbij(id: string) {
    this.http.post(
      `${this.apiUrl}/admin/zahtevi-kvota/${encodeURIComponent(id)}/odbij`,
      {},
      { withCredentials: true, responseType: 'text' }
    ).subscribe({
      next: () => {
        this.poruka = 'Zahtev je odbijen.';
        this.ucitajZahteve();
      },
      error: (e: HttpErrorResponse) => {
        this.greska = this.procitajGresku(e, 'Odbijanje nije uspelo.');
        this.cdr.detectChanges();
      }
    });
  }

  formatBajtova(bajtovi: number): string {
    const mb = this.bajtovaUMb(bajtovi);
    if (mb >= 100) {
      return `${Math.round(mb)} MB`;
    }
    const zaokruzeno = Math.round(mb * 10) / 10;
    return `${zaokruzeno} MB`;
  }

  private bajtovaUMb(bajtovi: number): number {
    return bajtovi / (1024 * 1024);
  }

  private procitajGresku(e: HttpErrorResponse, podrazumevano: string): string {
    if (e.status === 0) {
      return 'Nije moguce povezivanje sa serverom.';
    }
    if (typeof e.error === 'string' && e.error.trim()) {
      return e.error;
    }
    return podrazumevano;
  }
}