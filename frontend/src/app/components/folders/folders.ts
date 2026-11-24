
import {
  ChangeDetectorRef,
  Component,
  inject,
  OnInit
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';

interface Folder {
  id: string;
  naziv: string;
}

interface Datoteka {
  id: string;
  naziv: string;
  tip?: string;
  velicina?: number;
}

interface SadrzajOdgovor {
  folderi: Folder[];
  datoteke: Datoteka[];
}

interface BreadcrumbItem {
  id: string | null;
  naziv: string;
}

@Component({
  selector: 'app-folders',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './folders.html',
  styleUrls: ['./folders.css']
})
export class FoldersComponent implements OnInit {

  private router = inject(Router);
  private http = inject(HttpClient);
  private cdr = inject(ChangeDetectorRef);

  private apiFolderi = 'http://localhost:8080/api/folderi';
  private apiFajlovi = 'http://localhost:8080/api/fajlovi';

  ucitava = false;
  vlasnikId = '';

  trenutniRoditeljId: string | null = null;

  breadcrumb: BreadcrumbItem[] = [
    { id: null, naziv: 'Moj disk' }
  ];

  folderi: Folder[] = [];
  datoteke: Datoteka[] = [];

  noviNaziv = '';

  izabraniFajl: File | null = null;
  otpremanje = false;

  ngOnInit() {
    this.vlasnikId =
      localStorage.getItem('userId') ||
      localStorage.getItem('userEmail') ||
      '';

    if (!this.vlasnikId) {
      alert('Prvo se prijavite na /login.');
      this.router.navigate(['/login']);
      return;
    }

    this.ucitajSadrzaj();
  }

  private porukaSaServera(
    e: HttpErrorResponse,
    podrazumevano: string
  ): string {
    if (typeof e.error === 'string' && e.error.trim()) {
      return e.error;
    }

    if (e.error && typeof e.error.message === 'string') {
      return e.error.message;
    }

    return podrazumevano;
  }

  private validirajNaziv(naziv: string): string | null {
    const n = naziv.trim();

    if (!n) {
      return 'Naziv ne sme biti prazan.';
    }

    if (
      n.includes('/') ||
      n.includes('\\') ||
      n.includes('..')
    ) {
      return 'Naziv ne sme sadržati /, \\ ili ..';
    }

    if (n.length > 255) {
      return 'Naziv je predug (maksimalno 255 karaktera).';
    }

    return null;
  }

  ucitajSadrzaj() {
    let url =
      `${this.apiFolderi}/sadrzaj?vlasnikId=` +
      encodeURIComponent(this.vlasnikId);

    if (this.trenutniRoditeljId) {
      url +=
        `&roditeljId=` +
        encodeURIComponent(this.trenutniRoditeljId);
    }

    this.http.get<SadrzajOdgovor>(url, {
      withCredentials: true
    }).subscribe({
      next: (data) => {
        this.folderi = [...(data.folderi || [])];
        this.datoteke = [...(data.datoteke || [])];
        this.cdr.detectChanges();
      },
      error: (e) => {
        alert(
          this.porukaSaServera(
            e as HttpErrorResponse,
            'Greška pri učitavanju.'
          )
        );
      }
    });
  }

  kreirajFolder() {
    const greska = this.validirajNaziv(this.noviNaziv);

    if (greska) {
      alert(greska);
      return;
    }

    this.http.post<Folder>(
      this.apiFolderi,
      {
        naziv: this.noviNaziv.trim(),
        vlasnikId: this.vlasnikId,
        roditeljId: this.trenutniRoditeljId
      },
      { withCredentials: true }
    ).subscribe({
      next: () => {
        this.noviNaziv = '';
        this.ucitajSadrzaj();
      },
      error: (e) => {
        alert(
          this.porukaSaServera(
            e as HttpErrorResponse,
            'Greška pri kreiranju foldera.'
          )
        );
      }
    });
  }

  otvoriFolder(folder: Folder) {
    this.trenutniRoditeljId = folder.id;

    this.breadcrumb.push({
      id: folder.id,
      naziv: folder.naziv
    });

    this.ucitajSadrzaj();
  }

  idiNa(index: number) {
    this.breadcrumb = this.breadcrumb.slice(0, index + 1);

    this.trenutniRoditeljId =
      index === 0 ? null : this.breadcrumb[index].id;

    this.ucitajSadrzaj();
  }

  preimenujFolder(folder: Folder) {
    const unos = prompt('Novi naziv foldera:', folder.naziv);

    if (unos === null) return;

    const greska = this.validirajNaziv(unos);

    if (greska) {
      alert(greska);
      return;
    }

    this.http.put(
      `${this.apiFolderi}/${folder.id}/naziv`,
      {
        naziv: unos.trim(),
        vlasnikId: this.vlasnikId
      },
      { withCredentials: true }
    ).subscribe({
      next: () => this.ucitajSadrzaj(),
      error: (e) => {
        alert(
          this.porukaSaServera(
            e as HttpErrorResponse,
            'Greška pri preimenovanju foldera.'
          )
        );
      }
    });
  }

  preimenujDatoteku(datoteka: Datoteka) {
    const unos = prompt(
      'Novi naziv datoteke:',
      datoteka.naziv
    );

    if (unos === null) return;

    const greska = this.validirajNaziv(unos);

    if (greska) {
      alert(greska);
      return;
    }

    this.http.put(
      `${this.apiFajlovi}/${datoteka.id}/naziv`,
      {
        naziv: unos.trim(),
        vlasnikId: this.vlasnikId
      },
      { withCredentials: true }
    ).subscribe({
      next: () => this.ucitajSadrzaj(),
      error: (e) => {
        alert(
          this.porukaSaServera(
            e as HttpErrorResponse,
            'Greška pri preimenovanju datoteke.'
          )
        );
      }
    });
  }

  // 1.5 - Biranje fajla
  izaberiFajl(event: Event) {
    const input = event.target as HTMLInputElement;
    this.izabraniFajl = input.files?.[0] || null;
  }

  // 1.5 - Otpremanje novog fajla
  otpremiFajl() {
    if (this.otpremanje) return;

    if (!this.trenutniRoditeljId) {
      alert('Prvo otvorite folder.');
      return;
    }

    if (!this.izabraniFajl) {
      alert('Izaberite fajl.');
      return;
    }

    if (!this.vlasnikId) {
      alert('Prijavite se ponovo.');
      return;
    }

    const podaci = new FormData();

    podaci.append('vlasnikId', this.vlasnikId);
    podaci.append('folderId', this.trenutniRoditeljId);
    podaci.append('fajl', this.izabraniFajl);

    this.otpremanje = true;

    this.http.post(
      `${this.apiFajlovi}/otpremi`,
      podaci,
      { withCredentials: true }
    ).subscribe({
      next: () => {
        this.otpremanje = false;
        this.izabraniFajl = null;
        this.ucitajSadrzaj();
        alert('Datoteka je uspešno otpremljena.');
      },
      error: (e) => {
        this.otpremanje = false;
        alert(
          this.porukaSaServera(
            e as HttpErrorResponse,
            'Otpremanje nije uspelo.'
          )
        );
      }
    });
  }

  // 1.5 - Nova verzija postojece datoteke
  otpremiNovuVerziju(
    event: Event,
    datoteka: Datoteka
  ) {
    const input = event.target as HTMLInputElement;
    const fajl = input.files?.[0];

    if (!fajl || this.otpremanje) return;

    const podaci = new FormData();

    podaci.append('vlasnikId', this.vlasnikId);
    podaci.append('fajl', fajl);

    this.otpremanje = true;

    this.http.put(
      `${this.apiFajlovi}/${datoteka.id}/verzija`,
      podaci,
      { withCredentials: true }
    ).subscribe({
      next: () => {
        this.otpremanje = false;
        input.value = '';
        this.ucitajSadrzaj();
        alert('Nova verzija je uspešno sačuvana.');
      },
      error: (e) => {
        this.otpremanje = false;
        input.value = '';

        alert(
          this.porukaSaServera(
            e as HttpErrorResponse,
            'Nova verzija nije sačuvana.'
          )
        );
      }
    });
  }

  odjaviSe() {
    const apiUrl = 'http://localhost:8080/api/users';

    this.http.post(
      apiUrl + '/logout',
      {},
      { withCredentials: true, responseType: 'text' }
    ).subscribe({
      next: () => this.zavrsiLokalnuOdjava(),
      error: () => this.zavrsiLokalnuOdjava()
    });
  }

  private zavrsiLokalnuOdjava() {
    localStorage.removeItem('userId');
    localStorage.removeItem('userEmail');
    this.router.navigate(['/login']);
  }
}
