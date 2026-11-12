import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

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
  imports: [CommonModule, FormsModule],
  templateUrl: './folders.html',
  styleUrls: ['./folders.css']
})
export class FoldersComponent implements OnInit {
  private http = inject(HttpClient);
  private cdr = inject(ChangeDetectorRef);
  private apiFolderi = 'http://localhost:8082/api/folderi';
  private apiFajlovi = 'http://localhost:8082/api/fajlovi';

  vlasnikId = '';
  trenutniRoditeljId: string | null = null;
  breadcrumb: BreadcrumbItem[] = [{ id: null, naziv: 'Moj disk' }];
  folderi: Folder[] = [];
  datoteke: Datoteka[] = [];
  noviNaziv = '';

  ngOnInit() {
    this.vlasnikId = localStorage.getItem('userId') || '';
    if (!this.vlasnikId) {
      alert('Prvo se registrujte na /registracija.');
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
      error: (e) => alert(typeof e.error === 'string' ? e.error : 'Greška pri učitavanju.')
    });
  }

  kreirajFolder() {
    const naziv = this.noviNaziv.trim();
    if (!naziv) {
      alert('Unesite naziv foldera.');
      return;
    }
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
      error: (e) => alert(typeof e.error === 'string' ? e.error : 'Greška pri kreiranju.')
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
    const novi = prompt('Novi naziv foldera:', folder.naziv);
    if (!novi || !novi.trim()) {
      return;
    }
    this.http.put(`${this.apiFolderi}/${folder.id}/naziv`, {
      naziv: novi.trim(),
      vlasnikId: this.vlasnikId
    }).subscribe({
      next: () => this.ucitajSadrzaj(),
      error: (e) => alert(typeof e.error === 'string' ? e.error : 'Greška pri preimenovanju.')
    });
  }

  preimenujDatoteku(datoteka: Datoteka) {
    const novi = prompt('Novi naziv datoteke:', datoteka.naziv);
    if (!novi || !novi.trim()) {
      return;
    }
    this.http.put(`${this.apiFajlovi}/${datoteka.id}/naziv`, {
      naziv: novi.trim(),
      vlasnikId: this.vlasnikId
    }).subscribe({
      next: () => this.ucitajSadrzaj(),
      error: (e) => alert(typeof e.error === 'string' ? e.error : 'Greška pri preimenovanju.')
    });
  }
}
