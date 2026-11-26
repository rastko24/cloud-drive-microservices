
import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { APP_CONFIG } from '../../app.config';

@Component({
  selector: 'app-magic-link-prijava',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './magic-link-prijava.html',
  styleUrls: ['./magic-link-prijava.css']
})
export class MagicLinkPrijavaComponent {
  email = '';
  ucitava = false;
  poruka = '';
  greska = '';

  private http = inject(HttpClient);
  private cdr = inject(ChangeDetectorRef);
  private apiUrl = `${APP_CONFIG.gatewayApiUrl}/api/users/zatrazi-magic-link`;

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
      { responseType: 'text', withCredentials: true }
    ).subscribe({
      next: () => {
        this.ucitava = false;
        this.poruka = 'Ako nalog postoji, poslat je magični link na vašu e-mail adresu.';
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
