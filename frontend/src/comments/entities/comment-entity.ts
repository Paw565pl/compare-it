export interface CommentEntity {
  readonly id: number;
  readonly author: string | null;
  readonly text: string;
  readonly createdAt: string;
  readonly positiveRatingsCount: number;
  readonly negativeRatingsCount: number;
}
