import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Session {
  id: string;
  containerId: string;
  lastActiveAt: string;
  ttlSeconds: number;
  expired: boolean;
  status?: string; // ✅ Optional field
}

@Injectable({ providedIn: 'root' })
export class SessionService {
  private readonly apiUrl = 'http://localhost:8080/api/session';

  constructor(private http: HttpClient) {}

  startSession(image: string = 'alpine:latest'): Observable<Session> {
    return this.http.post<Session>(`${this.apiUrl}/start?image=${image}`, {});
  }

  listSessions(): Observable<Session[]> {
    return this.http.get<Session[]>(`${this.apiUrl}/list`);
  }

  // optional
  removeSession(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
