import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PromenaLozinke } from './promena-lozinke';

describe('PromenaLozinke', () => {
  let component: PromenaLozinke;
  let fixture: ComponentFixture<PromenaLozinke>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PromenaLozinke],
    }).compileComponents();

    fixture = TestBed.createComponent(PromenaLozinke);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
