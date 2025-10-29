import { TestBed } from '@angular/core/testing';

import { Terminal } from './terminal';

describe('Terminal', () => {
  let service: Terminal;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Terminal);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
