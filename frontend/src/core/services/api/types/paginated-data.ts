interface PaginationState {
  readonly size: number;
  readonly number: number;
  readonly totalElements: number;
  readonly totalPages: number;
}

export interface PaginatedData<T> {
  readonly content: T[];
  readonly page: PaginationState;
}
