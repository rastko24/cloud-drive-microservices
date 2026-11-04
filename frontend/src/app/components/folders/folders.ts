import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

interface Folder {
  id: string;
  naziv: string;
  vlasnikId: string;
  roditeljId: string | null;
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
  private apiBase = 'http://localhost:8082/api/folderi';

  vlasnikId = '';
  trenutniRoditeljId: string | null = null;
  breadcrumb: BreadcrumbItem[] = [{ id: null, naziv: 'Moj disk' }];
  folderi: Folder[] = [];
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
    let url = `${this.apiBase}/sadrzaj?vlasnikId=${encodeURIComponent(this.vlasnikId)}`;
    if (this.trenutniRoditeljId) {
      url += `&roditeljId=${encodeURIComponent(this.trenutniRoditeljId)}`;
    }
    this.http.get<Folder[]>(url).subscribe({
      next: (data) => (this.folderi = data),
      error: (e) => alert(typeof e.error === 'string' ? e.error : 'Greška pri učitavanju.')
    });
  }

  kreirajFolder() {
    const naziv = this.noviNaziv.trim();
    if (!naziv) {
      alert('Unesite naziv foldera.');
      return;
    }
    this.http.post(this.apiBase, {
      naziv,
      vlasnikId: this.vlasnikId,
      roditeljId: this.trenutniRoditeljId
    }).subscribe({
      next: () => {
        this.noviNaziv = '';
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
    this.trenutniRoditeljId = this.breadcrumb[index].id;
    this.ucitajSadrzaj();
  }
}