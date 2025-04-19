export interface TokenRefreshResponse {
  readonly access_token: string;
  readonly refresh_token: string;
  readonly id_token: string;
  readonly expires_in: number;
}
