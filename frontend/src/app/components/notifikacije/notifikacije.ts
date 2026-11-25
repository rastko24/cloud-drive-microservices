import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { APP_CONFIG } from '../../app.config';

export interface Notifikacija {
  id?: string;
  email: string;
  poruka: string;
  vremeKreiranja: string;
}

@Component({
  selector: 'app-notifikacije',
  imports: [CommonModule],
  templateUrl: './notifikacije.html',
  styleUrl: './notifikacije.css',
})
export class Notifikacije implements OnInit {
  notifikacije: Notifikacija[] = [];
  ucitavanje = true;
  greska = '';
  email = '';

  private readonly apiUrl = APP_CONFIG.notificationsApiUrl;

  constructor(
    private http: HttpClient,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.email =
      localStorage.getItem('userEmail') ||
      localStorage.getItem('email') ||
      '';

    if (!this.email.trim()) {
      this.greska = 'Nije pronađen email ulogovanog korisnika.';
      this.ucitavanje = false;
      this.cdr.detectChanges();
      return;
    }

    this.http
      .get<Notifikacija[]>(this.apiUrl, {
        params: { email: this.email.trim() },
      })
      .subscribe({
        next: (data) => {
          this.notifikacije = data ?? [];
          this.ucitavanje = false;
          this.cdr.detectChanges();
        },
        error: () => {
          this.greska = 'Greška pri učitavanju notifikacija sa servera.';
          this.ucitavanje = false;
          this.cdr.detectChanges();
        },
      });
  }

  nazadNaFoldere(): void {
    this.router.navigate(['/folderi']);
  }
}