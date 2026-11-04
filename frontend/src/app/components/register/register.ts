import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';

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

  private apiUrl = 'http://localhost:8081/api/users/register';
  private http = inject(HttpClient);

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

    const jakaLozinkaRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/;
    if (!jakaLozinkaRegex.test(this.lozinka)) {
      alert('Lozinka mora imati minimum 8 karaktera, jedno veliko slovo, jedan broj i jedan specijalni znak!');
      return;
    }

    const podaciZaRegistraciju = {
      email: this.email,
      lozinka: this.lozinka,
      ime: this.ime,
      prezime: this.prezime
    };

    this.http.post(this.apiUrl, podaciZaRegistraciju).subscribe({
		next: (odgovor: any) => {
		  console.log('Uspešna registracija:', odgovor);
		  localStorage.setItem('userId', odgovor.id);
		  localStorage.setItem('userEmail', odgovor.email);
		  alert('Uspešno ste se registrovali!');
		},
      error: (greska) => {
        console.error('Greška pri registraciji:', greska);
        const poruka = typeof greska.error === 'string' ? greska.error : 'Došlo je do problema.';
        alert('Greška: ' + poruka);
      }
    });
  }
}