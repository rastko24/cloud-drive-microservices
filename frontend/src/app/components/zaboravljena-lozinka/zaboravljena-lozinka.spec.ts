import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { ZaboravljenaLozinkaComponent } from './zaboravljena-lozinka';

describe('ZaboravljenaLozinka', () => {
  let component: ZaboravljenaLozinkaComponent;
  let fixture: ComponentFixture<ZaboravljenaLozinkaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ZaboravljenaLozinkaComponent],
      providers: [provideRouter([])]
    }).compileComponents();

    fixture = TestBed.createComponent(ZaboravljenaLozinkaComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
