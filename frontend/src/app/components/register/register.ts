import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router, RouterLink } from '@angular/router';
import { APP_CONFIG } from '../../app.config';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrls: ['./register.css']
})
export class RegisterComponent {
  ime: string = '';
  prezime: string = '';
  email: string = '';
  lozinka: string = '';
  potvrdaLozinke: string = '';

  prikaziPopupPotvrde: boolean = false;
  kodPotvrde: string = '';
  porukaPotvrde: string = '';
  greskaPotvrde: string = '';
  ucitavanje: boolean = false;

  private baseUrl = `${APP_CONFIG.usersApiUrl}/api/users`;
  private http = inject(HttpClient);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);

  registrujSe() {
    if (!this.ime.trim() || !this.prezime.trim() || !this.email.trim() || !this.lozinka.trim() || !this.potvrdaLozinke.trim()) {
      alert('Sva polja moraju biti popunjena!');
      return;
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(this.email)) {
      alert('Unesite ispravnu email adresu!');
      return;
    }

    if (this.lozinka !== this.potvrdaLozinke) {
      alert('Lozinke se ne poklapaju!');
      return;
    }

    const jakaLozinkaRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^a-zA-Z0-9]).{12,}$/;
    if (!jakaLozinkaRegex.test(this.lozinka)) {
      alert('Lozinka mora imati minimum 12 karaktera, veliko slovo, malo slovo, broj i specijalni znak!');
      return;
    }

    const podaciZaRegistraciju = {
      email: this.email,
      lozinka: this.lozinka,
      ponovljenaLozinka: this.potvrdaLozinke,
      ime: this.ime,
      prezime: this.prezime
    };

    this.ucitavanje = true;
    this.http.post(`${this.baseUrl}/register`, podaciZaRegistraciju).subscribe({
      next: (odgovor: any) => {
        this.ucitavanje = false;
        localStorage.setItem('userId', odgovor.id);
        localStorage.setItem('userEmail', odgovor.email);
        this.kodPotvrde = '';
        this.greskaPotvrde = '';
        this.porukaPotvrde = `Poslali smo 6-cifreni kod na ${this.email}.`;
        this.prikaziPopupPotvrde = true;
        this.cdr.detectChanges();
      },
      error: (greska) => {
        this.ucitavanje = false;
        this.cdr.detectChanges();
        const poruka = typeof greska.error === 'string' ? greska.error : 'Došlo je do problema.';
        alert('Greška: ' + poruka);
      }
    });
  }

  potvrdiKod() {
    if (!this.kodPotvrde.trim()) {
      this.greskaPotvrde = 'Unesite 6-cifreni kod sa mejla.';
      return;
    }

    this.ucitavanje = true;
    this.greskaPotvrde = '';

    this.http.post(`${this.baseUrl}/potvrdi-registraciju`, {
      email: this.email,
      kod: this.kodPotvrde.trim()
    }, { responseType: 'text' }).subscribe({
      next: () => {
        this.ucitavanje = false;
        this.prikaziPopupPotvrde = false;
        this.cdr.detectChanges();
        alert('Nalog je uspešno potvrđen! Sada se možete prijaviti.');
        this.router.navigate(['/login']);
      },
      error: (greska) => {
        this.ucitavanje = false;
        this.greskaPotvrde = typeof greska.error === 'string' ? greska.error : 'Pogrešan ili istekao kod.';
        this.cdr.detectChanges();
      }
    });
  }

  posaljiKodPonovo() {
    this.ucitavanje = true;
    this.greskaPotvrde = '';

    this.http.post(`${this.baseUrl}/ponovo-posalji-kod`, {
      email: this.email
    }, { responseType: 'text' }).subscribe({
      next: (odgovor) => {
        this.ucitavanje = false;
        this.porukaPotvrde = odgovor;
        this.cdr.detectChanges();
      },
      error: (greska) => {
        this.ucitavanje = false;
        this.greskaPotvrde = typeof greska.error === 'string' ? greska.error : 'Greška pri ponovnom slanju koda.';
        this.cdr.detectChanges();
      }
    });
  }

  zatvoriPopup() {
    this.prikaziPopupPotvrde = false;
    this.cdr.detectChanges();
  }
}