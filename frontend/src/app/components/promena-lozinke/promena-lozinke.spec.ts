import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { PromenaLozinkeComponent } from './promena-lozinke';

describe('PromenaLozinke', () => {
  let component: PromenaLozinkeComponent;
  let fixture: ComponentFixture<PromenaLozinkeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PromenaLozinkeComponent],
      providers: [provideRouter([])]
    }).compileComponents();

    fixture = TestBed.createComponent(PromenaLozinkeComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
