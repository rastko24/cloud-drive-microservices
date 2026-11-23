import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
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
  breadcrumb: BreadcrumbItem[] = [{ id: null, naziv: 'Moj disk' }];
  folderi: Folder[] = [];
  datoteke: Datoteka[] = [];
  noviNaziv = '';

  private nedozvoljenNaziv(naziv: string): boolean {
    return naziv.includes('/') || naziv.includes('\\') || naziv.includes('..');
  }

  

  private validirajNaziv(naziv: string): string | null {
    const n = naziv.trim();
    if (!n) {
      return 'Naziv ne sme biti prazan.';
    }
    if (this.nedozvoljenNaziv(n)) {
      return 'Naziv ne sme sadržati /, \\ ili ..';
    }
    if (n.length > 255) {
      return 'Naziv je predug (maksimalno 255 karaktera).';
    }
    return null;
  }

  private porukaSaServera(e: HttpErrorResponse, podrazumevano: string): string {
    if (typeof e.error === 'string' && e.error.trim()) {
      return e.error;
    }
    if (e.error && typeof e.error.message === 'string') {
      return e.error.message;
    }
    return podrazumevano;
  }

  odjaviSe() {
    localStorage.removeItem('userId');
    localStorage.removeItem('userEmail');
    this.router.navigate(['/login']);
  }

  ngOnInit() {
    this.vlasnikId = localStorage.getItem('userId') || localStorage.getItem('userEmail') || '';
    if (!this.vlasnikId) {
      alert('Prvo se prijavite na /login.');
      return;
    }
    this.ucitajSadrzaj();
  }

  ucitajSadrzaj() {
    let url = `${this.apiFolderi}/sadrzaj?vlasnikId=${encodeURIComponent(this.vlasnikId)}`;
    if (this.trenutniRoditeljId) {
      url += `&roditeljId=${encodeURIComponent(this.trenutniRoditeljId)}`;
    }
    this.http.get<SadrzajOdgovor>(url).subscribe({
      next: (data) => {
        this.folderi = [...(data.folderi || [])];
        this.datoteke = [...(data.datoteke || [])];
        this.cdr.detectChanges();
      },
      error: (e) =>
        alert(this.porukaSaServera(e as HttpErrorResponse, 'Greška pri učitavanju.'))
    });
  }

  kreirajFolder() {
    const greska = this.validirajNaziv(this.noviNaziv);
    if (greska) {
      alert(greska);
      return;
    }
    const naziv = this.noviNaziv.trim();

    this.http.post<Folder>(this.apiFolderi, {
      naziv,
      vlasnikId: this.vlasnikId,
      roditeljId: this.trenutniRoditeljId
    }).subscribe({
      next: (kreiran) => {
        this.noviNaziv = '';
        this.folderi = [...this.folderi, kreiran];
        this.ucitajSadrzaj();
      },
      error: (e) =>
        alert(this.porukaSaServera(e as HttpErrorResponse, 'Greška pri kreiranju.'))
    });
  }

  otvoriFolder(folder: Folder) {
    this.trenutniRoditeljId = folder.id;
    this.breadcrumb.push({ id: folder.id, naziv: folder.naziv });
    this.ucitajSadrzaj();
  }

  idiNa(index: number) {
    this.breadcrumb = this.breadcrumb.slice(0, index + 1);
    this.trenutniRoditeljId = index === 0 ? null : this.breadcrumb[index].id;
    this.ucitajSadrzaj();
  }

  preimenujFolder(folder: Folder) {
    const unos = prompt('Novi naziv foldera:', folder.naziv);
    if (unos === null) {
      return;
    }
    const greska = this.validirajNaziv(unos);
    if (greska) {
      alert(greska);
      return;
    }
    const novi = unos.trim();

    this.http.put(`${this.apiFolderi}/${folder.id}/naziv`, {
      naziv: novi,
      vlasnikId: this.vlasnikId
    }).subscribe({
      next: () => this.ucitajSadrzaj(),
      error: (e) =>
        alert(this.porukaSaServera(e as HttpErrorResponse, 'Greška pri preimenovanju.'))
    });
  }

  preimenujDatoteku(datoteka: Datoteka) {
    const unos = prompt('Novi naziv datoteke:', datoteka.naziv);
    if (unos === null) {
      return;
    }
    const greska = this.validirajNaziv(unos);
    if (greska) {
      alert(greska);
      return;
    }
    const novi = unos.trim();

    this.http.put(`${this.apiFajlovi}/${datoteka.id}/naziv`, {
      naziv: novi,
      vlasnikId: this.vlasnikId
    }).subscribe({
      next: () => this.ucitajSadrzaj(),
      error: (e) =>
        alert(this.porukaSaServera(e as HttpErrorResponse, 'Greška pri preimenovanju.'))
    });
  }
}