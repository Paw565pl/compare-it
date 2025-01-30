export interface PriceAlertEntity {
  readonly id: string;
  readonly productId: string;
  readonly productName: string;
  readonly targetPrice: number;
  readonly currentLowestPrice: number;
  readonly outletAllowed: boolean;
  readonly active: boolean;
  readonly createdAt: string;
  readonly lastNotificationSent: string;
}
