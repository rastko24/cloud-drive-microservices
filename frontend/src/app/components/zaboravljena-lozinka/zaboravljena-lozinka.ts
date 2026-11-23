import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-zaboravljena-lozinka',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './zaboravljena-lozinka.html',
  styleUrls: ['./zaboravljena-lozinka.css']
})
export class ZaboravljenaLozinkaComponent {
  email = '';
  ucitava = false;
  poruka = '';
  greska = '';

  private http = inject(HttpClient);
  private cdr = inject(ChangeDetectorRef);
  private apiUrl = 'http://localhost:8081/api/users/zatrazi-reset';

  posaljiZahtev() {
    this.greska = '';
    this.poruka = '';

    const email = this.email.trim();

    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      this.greska = 'Unesite ispravnu e-mail adresu.';
      return;
    }

    this.ucitava = true;

    this.http.post(
      this.apiUrl,
      { email },
      { responseType: 'text' }
    ).subscribe({
      next: () => {
        this.ucitava = false;
        this.poruka = 'Ako nalog postoji, poslat je link za resetovanje lozinke.';
        this.cdr.detectChanges();
      },
      error: () => {
        this.ucitava = false;
        this.greska = 'Zahtev nije uspeo. Proverite vezu sa serverom i pokušajte ponovo.';
        this.cdr.detectChanges();
      }
    });
  }
}