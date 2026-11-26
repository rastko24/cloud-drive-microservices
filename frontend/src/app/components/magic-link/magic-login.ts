
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { APP_CONFIG } from '../../app.config';

@Component({
  selector: 'app-magic-login',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './magic-login.html',
  styleUrls: ['./magic-link-prijava.css']
})
export class MagicLoginComponent implements OnInit {
  ucitava = true;
  greska = '';

  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private http = inject(HttpClient);
  private cdr = inject(ChangeDetectorRef);
  private apiUrl = `${APP_CONFIG.gatewayApiUrl}/api/users/magic-login`;

  ngOnInit() {
    const email = this.route.snapshot.queryParamMap.get('email');
    const token = this.route.snapshot.queryParamMap.get('token');

    if (!email || !token) {
      this.ucitava = false;
      this.greska = 'Link za prijavu nije potpun.';
      this.cdr.detectChanges();
      return;
    }

    this.http.post(
      this.apiUrl,
      { email, token },
      { responseType: 'text', withCredentials: true }
    ).subscribe({
      next: () => {
        this.ucitava = false;
        this.cdr.detectChanges();
        this.router.navigate(['/folderi']);
      },
      error: (err) => {
        this.ucitava = false;
        this.greska = typeof err?.error === 'string' && err.error
          ? err.error
          : 'Link za prijavu nije ispravan ili je istekao.';
        this.cdr.detectChanges();
      }
    });
  }
}
