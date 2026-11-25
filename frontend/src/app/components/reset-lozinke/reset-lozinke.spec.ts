import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { ResetLozinkeComponent } from './reset-lozinke';

describe('ResetLozinke', () => {
  let component: ResetLozinkeComponent;
  let fixture: ComponentFixture<ResetLozinkeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ResetLozinkeComponent],
      providers: [provideRouter([])]
    }).compileComponents();

    fixture = TestBed.createComponent(ResetLozinkeComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
