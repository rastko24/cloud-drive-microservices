import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { MagicLinkPrijavaComponent } from './magic-link-prijava';

describe('MagicLinkPrijavaComponent', () => {
  let component: MagicLinkPrijavaComponent;
  let fixture: ComponentFixture<MagicLinkPrijavaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MagicLinkPrijavaComponent],
      providers: [provideHttpClient(), provideRouter([])]
    }).compileComponents();

    fixture = TestBed.createComponent(MagicLinkPrijavaComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});