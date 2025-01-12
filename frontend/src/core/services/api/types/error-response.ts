export interface ErrorResponse {
  readonly timestamp: string;
  readonly status: number;
  readonly error: string;
  readonly message: string;
  readonly errors?: Record<string, string[]>;
}
