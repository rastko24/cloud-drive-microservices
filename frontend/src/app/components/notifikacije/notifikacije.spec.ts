import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { Notifikacije, Notifikacija } from './notifikacije';

describe('Notifikacije', () => {
  let component: Notifikacije;
  let fixture: ComponentFixture<Notifikacije>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    localStorage.clear();
    localStorage.setItem('userEmail', 'petrovicfilip112@gmail.com');

    await TestBed.configureTestingModule({
      imports: [Notifikacije],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Notifikacije);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    await fixture.whenStable();
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should create', () => {
    fixture.detectChanges();
    const req = httpMock.expectOne(
      (r) =>
        r.url === 'http://localhost:8080/api/notifikacije' &&
        r.params.get('email') === 'petrovicfilip112@gmail.com'
    );
    const mockData: Notifikacija[] = [
      {
        id: '1',
        email: 'petrovicfilip112@gmail.com',
        poruka: 'Nova notifikacija - sve radi kako treba.',
        vremeKreiranja: '2026-11-25T10:27:35.240Z',
      },
    ];
    req.flush(mockData);
    fixture.detectChanges();

    expect(component).toBeTruthy();
    expect(component.notifikacije.length).toBe(1);
  });
});