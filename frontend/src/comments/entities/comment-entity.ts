export interface CommentEntity {
  readonly id: string;
  readonly author: string | null;
  readonly text: string;
  readonly createdAt: string;
  readonly positiveRatingsCount: number;
  readonly negativeRatingsCount: number;
  readonly isRatingPositive: boolean | null;
}
