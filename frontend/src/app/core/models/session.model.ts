export interface Session {
  id: string;
  containerId: string;
  lastActiveAt: string;
  ttlSeconds: number;
  expired: boolean;
}
