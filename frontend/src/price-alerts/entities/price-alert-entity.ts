export interface PriceAlertEntity {
  readonly id: string;
  readonly productId: string;
  readonly productName: string;
  readonly targetPrice: number;
  readonly currentLowestPrice: number;
  readonly isOutletAllowed: boolean;
  readonly isActive: boolean;
  readonly createdAt: string;
  readonly lastNotificationSent: string | null;
}
